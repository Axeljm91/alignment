/*
* Application : iAlign
* Filename : Main.java
* Author : Fran�ois Vander Stappen
* Date : 22/10/2010
* Company : IBA
* Version : 0.4.2
*/

package com.iba.ialign;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import javax.swing.*;
import java.net.URL;


public class Main {
	/**
	 * The logger. See http://logging.apache.org/log4j/1.2/index.html for more information.
	 */
    private static Logger log=Logger.getLogger(new Throwable().getStackTrace()[0].getClassName());
	
    
	static
	{
		URL log4jUrl=ClassLoader.getSystemResource("xml/log4jwithoutGUI.xml");
		//URL log4jUrl=Main.class.getClassLoader().getResource("xml/log4jwithoutGUI.xml");
		DOMConfigurator.configure(log4jUrl);
	}
	
	public static void main(String[] args) {

		final Controller controller;
		boolean isCommandNotifPort = false ;
		int nPort = 16540;
		
		if(args.length > 0)
		{
			System.out.println("first parameter is " + args[0]);
			
			String[] notifPort= args[0].split("=") ;
			if (notifPort[0].equals("comm.notif.port"))
			{
				nPort = Integer.parseInt(notifPort[1]);
				isCommandNotifPort = true ;
				System.out.println("comm.notif.port is " + nPort);
			}
			else
			{
				
				System.out.println("ignore first parameter in startup command line, please make sure the parameter name as this: comm.F.port=18386");
			}
		}
		System.out.println(ClassLoader.getSystemResource(""));
		try 
		{
			//String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
			String lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
			UIManager.setLookAndFeel(lookAndFeel);
			System.out.println("Look&Feel : " + lookAndFeel);
		} 
		catch (ClassNotFoundException e) 
		{
			log.error("Error changing the look and feel:"+e.getMessage());
		} 
		catch (InstantiationException e) 
		{
			log.error("Error changing the look and feel:"+e.getMessage());
		} 
		catch (IllegalAccessException e) 
		{
			log.error("Error changing the look and feel:"+e.getMessage());
		} 
		catch (UnsupportedLookAndFeelException e) 
		{
			log.error("Error changing the look and feel:"+e.getMessage());
		}	

		//SwingUtilities.invokeLater(new Runnable() {
		//	@Override
		//	public void run() 
		//	{
		
		try{
			controller = new Controller();
            System.out.println("Hello, Controller.");
            if(isCommandNotifPort)
            {
            	controller.setNotifPort(nPort);
            }
			Gui mainFrame = new Gui(controller);
			mainFrame.setTitle("Service Beam GUI (" + controller.getSiteName() + " -- Version 1.4.1)");
		}catch (Exception e){
			System.out.println("Error creating Controller :"+e);
            e.printStackTrace();
		}

	}

}
