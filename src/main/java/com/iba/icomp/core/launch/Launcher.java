// ///////////////////////////////////////////////////////////////////
// Copyright (c) by Ion Beam Applications S.A.
// All rights reserved
//
// Rue du cyclotron, 3
// B-1348 Louvain-la-Neuve
// Tel : +32.10.47.58.11
// ///////////////////////////////////////////////////////////////////

package com.iba.icomp.core.launch;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.iba.icomp.core.config.DefaultPropertiesLoader;
import com.iba.icomp.core.config.Property;
import com.iba.icomp.core.service.Service;
import com.iba.icomp.core.service.ServiceController;
import com.iba.icomp.core.service.ServiceListener;
import com.iba.icomp.core.util.*;

/**
 * Class launching an application based on a Spring configuration file.
 * <p>
 * If no arguments are given, the configuration file must be accessible in the class-path with the path
 * <code>config/container.xml</code>. This configuration file should contain a bean named
 * <code>startupController</code> that must be an instance of {@link ServiceController}.
 * <p>
 * If the container is named <code>container.xml</code> then there must be a file in the class-path named
 * <code>container.properties</code>.
 * <p>
 * If the property <code>launch.listener</code> exists, it must contain the name of the class being an
 * implementation of {@link LaunchListener}. The launcher will notify that implementation at some times.
 * <p>
 * If the property <code>debug-launch</code> is set to <code>true</code>, information about the loaded files
 * and the system properties will be printed out.
 * @see #DEFAULT_CONFIG_FILE
 * @see #main(String[])
 * @see LaunchListener
 */
public class Launcher extends DebugAble
{
    /** The default configuration file. */
    public static final String DEFAULT_CONFIG_FILE = "config/container.xml";
    /** The property to debug the launcher. */
    public static final String DEBUG_PROPERTY = "debug-launch";
    /** The property for configuring the loggers. */
    public static final String LOG_CONFIG_PROPERTY = "icomp.log.configurator";
    /** The prefix for properties that activate a Spring profile. */
    public static final String PROFILE_PREFIX = "config.";
    /** The name of the IComP property source. */
    public static final String ICOMP_PROPERTY_SOURCE = "icomp";

    /**
     * Initializes the launcher.
     * @param pConfig the array of configuration (spring) files, the first one is considered as the container,
     *        not <code>null</code> and not empty.
     * @throws LaunchException if the container is not valid, because it does not have an associated properties
     *         file.
     */
    public Launcher(String... pConfig) throws LaunchException
    {
        this(null, pConfig);
    }

    /**
     * Initializes the launcher.
     * @param pClassLoader the class-loader to use, may be <code>null</code>.
     * @param pConfig the array of configuration (spring) files, the first one is considered as the container,
     *        not <code>null</code> and not empty.
     * @throws LaunchException if the container is not valid, because it does not have an associated properties
     *         file.
     */
    public Launcher(ClassLoader pClassLoader, String... pConfig) throws LaunchException
    {
        this(System.out, pClassLoader, pConfig);
    }

    /**
     * Initializes the launcher.
     * @param pDebugOutput the debug output, may be <code>null</code>.
     * @param pClassLoader the class-loader to use, may be <code>null</code>.
     * @param pConfig the array of configuration (spring) files, the first one is considered as the container,
     *        not <code>null</code> and not empty.
     * @throws LaunchException if the container is not valid, because it does not have an associated properties
     *         file.
     */
    public Launcher(PrintStream pDebugOutput, ClassLoader pClassLoader, String... pConfig) throws LaunchException
    {
        mClassLoader = pClassLoader;
        mConfig = pConfig.clone();
        mContainerFile = pConfig[0];
        mListeners = new ArrayList<LaunchListener>();
        // To allow debugging
        if (isDebugLaunch())
        {
            setDebugOutput(pDebugOutput);
            addListener(new PropertySpy(pDebugOutput));
        }
        initialize();
    }

    /**
     * Initializes the properties of the container.
     * @throws LaunchException if the container is not valid, because it does not have an associated properties
     *         file.
     */
    private void initialize() throws LaunchException
    {
        debug("Launching container configuration '%s'.", mContainerFile);
        final String propFile = getRelatedFile(mContainerFile, ".properties");
        debug("Loading properties file '%s'.", propFile);
        // if (System.getProperty("container.propertiesFile") == null)
        // {
        // Must be the same as the file which was used to load the properties
        System.setProperty("container.propertiesFile", propFile);
        // }
        // initializes the logger
        mContainerProperties = null;
        try
        {
            final DefaultPropertiesLoader loader;
            loader = new DefaultPropertiesLoader(System.getProperties());
            loader.setDebugOutput(getDebugOutput());
            loader.load(propFile, mClassLoader);
            mContainerProperties = loader.getProperties();
        }
        catch (IOException e)
        {
            throw new LaunchException("Container is missing a valid" + " properties file.", e);
        }

        // creates a listener if there is one defined
        if (mContainerProperties.getProperty("launch.listener") != null)
        {
            for (final String l : Strings.split(',', mContainerProperties.getProperty("launch.listener")))
            {
                addListener(createInstanceOf(l, LaunchListener.class));
                debug("Added launch listener '%s'.", l);
            }
        }
    }

    /**
     * Initializes the Log4J logging system.
     * <p>
     * If a <code>icomp.log.configurator</code> property is defined, it represents the name of the class that
     * is used to configure the loggers. It it is not defined the loggers will be created from the properties
     * of the container.
     * @throws LaunchException if the loggers could not be initialized.
     */
    public void initializeLoggers() throws LaunchException
    {
        try
        {
            final String c = mContainerProperties.getProperty(LOG_CONFIG_PROPERTY);
            if (c == null)
            {
                debug("Configuring Log4J from properties.");
                org.apache.log4j.PropertyConfigurator.configure(mContainerProperties);
                return;
            }
            debug("Configuring Log4J from '%s'.", c);
            createInstanceOf(c, LaunchConfigurator.class).configure(this);
        }
        finally
        {
            if (Thread.interrupted())
            {
                Logger.getLogger().warn(
                        "Interrupted flag is set. As this is unwanted, we clear it and continue with the startup of the container.");
            }
        }
    }

    /**
     * Gets the list of active Spring profiles.
     * @param pProperties The input properties
     * @return The active Srping profiles.
     */
    public static List<String> getActiveProfiles(Properties pProperties)
    {
        List<String> profiles = new ArrayList<String>();

        profiles.add("default");

        for (Map.Entry<Object, Object> prop : pProperties.entrySet())
        {
            String propName = (String) prop.getKey();
            String propValue = (String) prop.getValue();
            if (propName.startsWith(PROFILE_PREFIX))
            {
                profiles.add(propName.substring(PROFILE_PREFIX.length()) + "=" + propValue);
            }
        }

        return profiles;
    }

    /**
     * Creates a bean-factory initializing the corresponding {@link ConfigurableEnvironment}. All properties
     * starting of the form config.xxx are used to activate the xxx bean profile.
     * @param pProperties the properties used in the {@link ConfigurableEnvironment}, not <code>null</code>.
     * @param pClassLoader the class loader, may be <code>null</code>.
     * @param pConfig the configuration of the bean-factory.
     * @return the bean-factory.
     * @throws BeansException if the creation of the bean-factory failed.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static ClassPathXmlApplicationContext createBeanFactory(Properties pProperties, ClassLoader pClassLoader,
                                                                   String... pConfig)
    {
        final ClassPathXmlApplicationContext beanFactory;
        beanFactory = new ClassPathXmlApplicationContext();
        beanFactory.setConfigLocations(pConfig);

        if (pClassLoader != null)
        {
            beanFactory.setClassLoader(pClassLoader);
        }

        configureBeanFactory(beanFactory, pProperties);

        beanFactory.refresh();
        beanFactory.start();

        return beanFactory;
    }

    /**
     * Configure one application context the IComP way:
     * <ol>
     * <li>Activate a profile for each property starting with '.config'</li>
     * <li>Engages a property placeholder configurer for each placeholder starting with '${prop:'.</li>
     * </ol>
     * @param pApplicationContext The application context to configure.
     * @param pProperties The list of properties.
     */
    public static void configureBeanFactory(ConfigurableApplicationContext pApplicationContext, Properties pProperties)
    {
        List<String> profiles = getActiveProfiles(pProperties);
        pApplicationContext.getEnvironment().setActiveProfiles(profiles.toArray(new String[profiles.size()]));

        MutablePropertySources sources = pApplicationContext.getEnvironment().getPropertySources();
        MapPropertySource mps = new MapPropertySource(ICOMP_PROPERTY_SOURCE, (Map) pProperties);
        sources.addFirst(mps);

        PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
        pspc.setPlaceholderPrefix("${prop:");
        pspc.setProperties(pProperties);
        pspc.setBeanName("PropertySourcesPlaceholderConfigurer");
        pspc.setEnvironment(pApplicationContext.getEnvironment());
        pApplicationContext.addBeanFactoryPostProcessor(pspc);
    }

    /**
     * Creates a bean-factory initializing the corresponding {@link PropertyPlaceholderConfigurer}.
     * @return the bean-factory.
     * @throws BeansException if the creation of the bean-factory failed.
     * @see #createBeanFactory(Properties, ClassLoader, String...)
     */
    public ClassPathXmlApplicationContext createBeanFactory()
    {
        return createBeanFactory(mContainerProperties, mClassLoader, mConfig);
    }

    /**
     * TODO: could be made public, or move to some util classes
     * @return true if only a formal classpath is in use by this process:
     *         <ul>
     *         <li>only jar files (no folders, no workspace)</li>
     *         <li>only released jars (no SNAPSHOT)</li>
     *         <li>exactly 4 jars (expected site/process/icomp/cots)</li>
     *         <li>only local jars (no path)</li>
     *         </ul>
     */
    public static boolean checkFormalClasspath()
    {
        boolean result = true; // optimistic approach!
        Logger logger = Logger.getLogger(Launcher.class);

        logger.info(System.getenv().toString());
        String classpath = System.getProperty("java.class.path");
        final String[] classPathEntries = Strings.split(File.pathSeparatorChar, classpath);

        if (classPathEntries.length != 4)
        {
            logger.warn("Classpath " + classpath + " does not contain exactly the 4 entries");
            result = false;
        }

        // Design note: check all entries, for detailed feedback( not fast-abort). But for each entry, use
        // fast-abort (for performance reason) - and faster checks first: if one entry is invalid, we don't need
        // to know all reasons why it is invalid: easy to find out by the user
        for (final String cp : classPathEntries)
        {
            if (!cp.endsWith(".jar"))
            {
                logger.warn("Classpath element " + cp + " is not a jar");
                result = false;
                continue;
            }
            if (cp.contains("/") || cp.contains("\\"))
            {
                logger.warn("Classpath element " + cp + " contains a path (or a local folder) - Only local jars file are allowed.");
                result = false;
                continue;
            }
            if (!new File(cp).exists())
            {
                logger.warn("Classpath element " + cp + " points to missing file");
                result = false;
                continue;
            }
            if (!new File(cp).isFile())
            {
                logger.warn("Classpath element " + cp + " points to a folder");
                result = false;
                continue;
            }
            final Version v = Version.versionOfZip(cp, Version.ASSEMBLY_VERSION);
            if (v == null)
            {
                logger.warn("Classpath element " + cp + " is not a valid, released assembly");
                result = false;
                continue;
            }
            else
            {
                logger.info("Classpath element %s: Version= %s:%s:%s (%s-%s)", cp, v.getGroupId(), v.getArtifactId(), v.getVersion(),
                        v.getScmIdentifier(), v.getScmDate());
            }
            // TBD: also check naming (sitexxx, processxxx, icompxxx, cotsxxx???)
        }
        if (result)
        {
            logger.debug("Classpath " + classpath + " is a formal classpath -> OK!");
        }
        return result;
    }

    /** Logs the version of the assembly. */
    public void logVersion()
    {
        // Get class-path (should never be null)
        final String[] classPath = Strings.split(File.pathSeparatorChar, System.getProperty("java.class.path"));
        debug("Assembly class-path is %s.", Arrays.asList(classPath));
        checkFormalClasspath();
    }

    private void logHostName()
    {
        try
        {
            InetAddress addr = InetAddress.getLocalHost();
            Logger.getLogger().info("Running on host=%s, IP address=%s", addr.getHostName(), addr.getHostAddress());
        }
        catch (UnknownHostException e)
        {
            Logger.getLogger().info("Cannot get host name.", e);
        }
    }

    /**
     * Launches the container.
     * @throws LaunchException if a parameter is not valid (e.g. properties file, <code>launch.listener</code>
     *         ).
     */
    public void launch() throws LaunchException
    {
        launch(true);
    }

    /**
     * Launches the container.
     * @param pStart whether the startup controller must be started.
     * @throws LaunchException if a parameter is not valid (e.g. properties file, <code>launch.listener</code>
     *         ).
     */
    public void launch(boolean pStart) throws LaunchException
    {
        final LoggerShutdownService shutdownLogging = new LoggerShutdownService();
        shutdownLogging.setName("shutdownLogging");
        shutdownLogging.start();
        // The initialization of the loggers should be before anything else
        initializeLoggers();
        // Log the version of the assembly
        logVersion();
        logHostName();
        // And start up the container
        fireContainerStarting();
        // Creates the application context
        mBeanFactory = createBeanFactory();
        // Get the container bean
        mStartupController = mBeanFactory.getBean("startupController", ServiceController.class);
        mStartupController.addServiceListener(new ServiceListener()
        {
            @Override
            public void serviceStopped(Service pService)
            {
                if (pService == mStartupController) // NOSONAR
                {
                    mBeanFactory.close();
                }
            }

            @Override
            public void serviceStarted(Service pService)
            { // NOSONAR Intentionally left empty
            }
        });
        mStartupController.addServiceListener(new ServiceListener()
        {
            @Override
            public void serviceStopped(Service pService)
            {
                shutdownLogging.stop();
            }

            @Override
            public void serviceStarted(Service pService)
            {
                //Nothing to do here.
            }
        });
        // Start the container
        if (pStart)
        {
            mStartupController.start();
        }
        // Notifies the listener that the container is started
        fireContainerStarted();
    }

    /**
     * Returns the class-loader of this launcher.
     * @return the class loader, may be <code>null</code>.
     */
    public ClassLoader getClassLoader()
    {
        return mClassLoader;
    }

    /**
     * Returns the properties of the initialized container.
     * @return the properties.
     */
    public Properties getContainerProperties()
    {
        return mContainerProperties;
    }

    /**
     * Returns the container file.
     * @return the container file, not <code>null</code>.
     */
    public String getContainerFile()
    {
        return mContainerFile;
    }

    /**
     * Returns the configuration files of the container.
     * @return the configuration files.
     */
    public String[] getConfig()
    {
        return mConfig.clone();
    }

    /**
     * Returns the bean-factory of the launched container.
     * @return the bean-factory or <code>null</code> if the container is not started.
     */
    public BeanFactory getBeanFactory()
    {
        return mBeanFactory;
    }

    /**
     * Returns the start-up controller of the launched container.
     * @return the controller or <code>null</code> if the container is not started.
     */
    public ServiceController getStartupController()
    {
        return mStartupController;
    }

    /**
     * Returns the listeners of the launch process.
     * @return the listeners.
     */
    public LaunchListener[] getListeners()
    {
        return mListeners.toArray(new LaunchListener[mListeners.size()]);
    }

    /**
     * Adds a listener to the launch process.
     * @param pListener the listener, not <code>null</code>.
     */
    public void addListener(LaunchListener pListener)
    {
        mListeners.add(pListener);
    }

    /** Fires the starting event to the launch listeners. */
    protected void fireContainerStarting()
    {
        for (final LaunchListener l : getListeners())
        {
            l.containerStarting(mContainerProperties);
        }
    }

    /** Fires the started event to the launch listeners. */
    protected void fireContainerStarted()
    {
        for (final LaunchListener l : getListeners())
        {
            l.containerStarted(mContainerProperties);
        }
    }

    /**
     * Loads the system properties from the properties files of the command line.
     * @param pParser the command line parser, not <code>null</code>.
     * @param pDebugOutput A stream to write debug message, not <code>null</code>.
     * @throws IOException if one of the properties files could not be found.
     */
    protected static void loadSystemPropertiesFiles(CLineParser pParser, PrintStream pDebugOutput) throws IOException
    {
        final ResourceLoader rl = new DefaultResourceLoader(Launcher.class.getClassLoader());
        for (final String c : pParser.getArguments("config", 'C', false))
        {
            Resource res = rl.getResource(c);
            pDebugOutput.println("Load config file " + res.getURL());
            Map<String, Property> p = DefaultPropertiesLoader.loadFrom(res);
            for (final Map.Entry<String, Property> e : p.entrySet())
            {
                System.setProperty(e.getKey(), e.getValue().getValue());
            }
        }
    }

    /**
     * Loads the system properties given on the command line.
     * @param pParser the command line parser, not <code>null</code>.
     */
    protected static void loadSystemProperties(CLineParser pParser)
    {
        for (final String d : pParser.getArguments("define", 'D', false))
        {
            final int idx = d.indexOf('=');
            if (idx == -1)
            {
                System.setProperty(d, "");
            }
            else
            {
                System.setProperty(d.substring(0, idx), d.substring(idx + 1));
            }
        }
    }

    /**
     * The method to launch the application. The arguments are
     * <code>[options] container [options|spring files]</code> where
     * <ul>
     * <li><code>options</code>:
     * <ul>
     * <li><code>-Dkey=value</code>: definition of a system property</li>
     * <li><code>-Cfile</code>: loads the given properties file</li>
     * </ul>
     * </li>
     * <li><code>container</code>: the spring file containing the container, this file will be used to deduce
     * the associated property file.</li>
     * <li><code>spring files</code>: the additional spring files for instantiation the container.</li>
     * </ul>
     * <p>
     * <strong>Precedence of property values</strong>
     * <p>
     * Property values coming from the command line (-C, -D) have precedence over the ones from
     * container.properties.
     * <p>
     * On the command line :
     * <ol>
     * <li>-D have precedence over -C.</li>
     * <li>Amongst -D, if the same property appears twice, the rightmost one has precedence.</li>
     * <li>-C files are looked for on the classpath using the regular Java class loader mechanism.</li>
     * <li>If two -C file contain the same property, the rightmost on the command line has precedence.</li>
     * <li>The include property has no particular meaning inside a -C file. If it refers to another property
     * file, that file will not be processed.</li>
     * <li>It is possible to define a -D whose value is a reference to another property : xxx=${prop:yyy}. You
     * 'just' have to escape correctly the dollar sign.</li>
     * </ol>
     * <p>
     * Properties coming from container.properties:
     * <ol>
     * <li>Files referenced inside the include property are read after the content of the current file.</li>
     * <li>If the include property contains several files, these are read from left to right.</li>
     * <li>When a property is defined more than once, the first value wins.</li>
     * </ol>
     * Attention : environment variables can be used by Spring but are not visible to this property resolution
     * mechanism.
     * @param pArgs the arguments, if no container is present, the default container location will be used.
     * @throws Exception if the container could not be launched.
     */
    public static void main(String[] pArgs) throws Exception
    {
        PrintStream out = System.out;
        main(out, pArgs);
    }

    /**
     * Configure global variables from command line arguments.
     * @param pDebugOutput the debug output of the launcher, not <code>null</code>.
     * @param pArgs the arguments.
     * @return the list of Spring configuration class-paths, if none is present, the default container location
     *         will be used.
     * @throws IOException if the container could not be launched.
     */
    public static List<String> readConfig(PrintStream pDebugOutput, String[] pArgs) throws IOException
    {
        final CLineParser p = new CLineParser(pArgs);
        loadSystemPropertiesFiles(p, pDebugOutput);
        loadSystemProperties(p);
        // Force illegal configuration file for log4j to ignore log4j.properties
        System.setProperty("log4j.defaultInitOverride", "true");
        System.setProperty("log4j.configuration", "<null>");
        //
        logArguments(pDebugOutput, pArgs);
        final List<String> config = p.getArguments(false);
        if (config.isEmpty())
        {
            config.add(DEFAULT_CONFIG_FILE);
        }
        return config;
    }

    /**
     * The method to launch the application.
     * @param pDebugOutput the debug output of the launcher, not <code>null</code>.
     * @param pArgs the arguments, if no container is present, the default container location will be used.
     * @throws Exception if the container could not be launched.
     * @see #main(String[])
     */
    public static void main(PrintStream pDebugOutput, String[] pArgs) throws Exception
    {
        List<String> config = readConfig(pDebugOutput, pArgs);
        final Launcher launcher = new Launcher(pDebugOutput, null, config.toArray(new String[config.size()]));

        launcher.launch();
    }

    /**
     * Returns a related file for the container.
     * @param pContainer the container file, not <code>null</code>.
     * @param pExtension the extension of the file, not <code>null</code>.
     * @return the name of the related file.
     */
    public static String getRelatedFile(String pContainer, String pExtension)
    {
        final String extension = ".xml";
        return (pContainer.endsWith(extension) ? pContainer.substring(0, pContainer.length() - extension.length()) : pContainer)
                + pExtension;
    }

    /**
     * Log the arguments of the launcher if debugging is enabled.
     * @param pDebugOutput the debug output, not <code>null</code>.
     * @param pArgs the arguments, not <code>null</code>.
     */
    private static void logArguments(PrintStream pDebugOutput, String[] pArgs)
    {
        if (isDebugLaunch())
        {
            pDebugOutput.println("Launcher arguments: " + Arrays.asList(pArgs));
        }
    }

    /**
     * Returns whether the container launch should be debugged.
     * @return <code>true</code> if the launch should be debugged, <code>false</code> otherwise.
     */
    public static boolean isDebugLaunch()
    {
        return "true".equals(System.getProperty(DEBUG_PROPERTY));
    }

    /**
     * Creates an instance of the given class.
     * @param <T> the type of the class.
     * @param pImplementationClass the implementation class name, not <code>null</code>.
     * @param pInterfaceClass the interface class, not <code>null</code>.
     * @return the implementation instance, not <code>null</code>.
     * @throws LaunchException if the class could not be instantiated.
     */
    public static <T> T createInstanceOf(String pImplementationClass, Class<T> pInterfaceClass) throws LaunchException
    {
        try
        {
            final Class<?> c = Class.forName(pImplementationClass);
            if (!pInterfaceClass.isAssignableFrom(c))
            {
                throw new LaunchException("The implementation '" + pImplementationClass + "' is not a '" + pInterfaceClass.getName()
                        + "'.");
            }
            return pInterfaceClass.cast(c.newInstance());
        }
        catch (ClassNotFoundException e)
        {
            throw new LaunchException("The implementation '" + pImplementationClass + "' of interface '" + pInterfaceClass.getName()
                    + "' is not present in the" + " class-path.", e);
        }
        catch (IllegalAccessException e)
        {
            throw new LaunchException("The implementation '" + pImplementationClass + "' of interface '" + pInterfaceClass.getName()
                    + "' is not accessible.", e);
        }
        catch (InstantiationException e)
        {
            throw new LaunchException("The implementation '" + pImplementationClass + "' of interface '" + pInterfaceClass.getName()
                    + "' could not be instantiated.", e);
        }
    }

    /** The (spring) configuration. */
    private final String[] mConfig;
    /** The container (spring) file. */
    private final String mContainerFile;
    /** The class-loader to use. */
    private final ClassLoader mClassLoader;
    /** The listener. */
    private final List<LaunchListener> mListeners;
    /** The properties of the container. */
    private Properties mContainerProperties;
    /** The bean-factory. */
    private ClassPathXmlApplicationContext mBeanFactory;
    /** The start-up controller. */
    private ServiceController mStartupController;
}
