/*
* Application : LookupAnalyser
* Filename : LogPanel.java
* Author : Henrotin S�bastien
* Date : 06/04/2010
* Company : IBA
* Version : 0.1
*/

package com.iba.ialign;

import com.iba.ialign.common.IbaColors;
import com.iba.ialign.common.TextPaneAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class LogPanel extends JPanel
{
	/**
	 * The logger. See http://logging.apache.org/log4j/1.2/index.html for more information.
	 */
	private static Logger log=Logger.getLogger(new Throwable().getStackTrace()[0].getClassName());

	private JTextPane textPane;
	
	public LogPanel()
	{
		this.textPane=new JTextPane();
        textPane.setEditable(false);
        textPane.setBackground(IbaColors.LT_GRAY);
		JScrollPane scrollPane=new JScrollPane(this.textPane);

		this.setLayout(new BorderLayout());

		this.add(scrollPane,BorderLayout.CENTER);
        this.setBackground(IbaColors.BT_GRAY);
				
		//setLayout(new BorderLayout());
		//this.add(area,BorderLayout.CENTER);
		TextPaneAppender.setTextArea(textPane);
		
			
		Properties logProperties = new Properties();

		logProperties.put("log4j.rootLogger", "INFO, CONSOLE, TEXTAREA");
		logProperties.put("log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender"); // A standard console appender
		logProperties.put("log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout"); //See: http://logging.apache.org/log4j/docs/api/org/apache/log4j/PatternLayout.html
		logProperties.put("log4j.appender.CONSOLE.layout.ConversionPattern", "%d{HH:mm:ss} [%12.12t] %5.5p %40.40c: %m%n");


		logProperties.put("log4j.appender.TEXTAREA", "com.iba.ialign.common.TextPaneAppender");  // Our custom appender
		logProperties.put("log4j.appender.TEXTAREA.layout", "org.apache.log4j.PatternLayout"); //See: http://logging.apache.org/log4j/docs/api/org/apache/log4j/PatternLayout.html
		logProperties.put("log4j.appender.TEXTAREA.layout.ConversionPattern", "%d{HH:mm:ss} %5.5p %40.40c: %m%n");
		PropertyConfigurator.configure(logProperties);
	}
	@Override
	public Dimension getPreferredSize() 
	{
		return new Dimension(this.getSize().width,100);
	}
	
	public class myTextArea extends JTextArea 
	{
		public myTextArea(int rows, int cols) 
		{
			super(rows,cols);
		}
		@Override
		public void append(String text) 
		{
			super.append(text);
			this.setCaretPosition(this.getCaretPosition()+text.length());
		}
	}
}
