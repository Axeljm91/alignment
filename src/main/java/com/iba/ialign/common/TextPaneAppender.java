/*
* Application : LookupAnalyser
* Filename : TextAreaAppender.java
* Author : Henrotin S�bastien
* Date : 07/04/2010
* Company : IBA
* Version : 0.1
*/

package com.iba.ialign.common;

import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;

/**
 * Simple example of creating a Log4j appender that will write to a JTextArea. This class is referenced in the log4j.xml file, configurating log4j.
 */
public class TextPaneAppender extends WriterAppender 
{
	/**
	 * The logger. See http://logging.apache.org/log4j/1.2/index.html for more information.
	 */
	private static Logger log=Logger.getLogger(new Throwable().getStackTrace()[0].getClassName());

	/**
	 *  The JTextArea in which for the logging information will appear.
	 */
	private static JTextPane jTextPane= null;
	
	/**
	 *  Set the target JTextArea for the logging information to appear.
	 */
	public static void setTextArea(JTextPane jTextPane)
	{
		TextPaneAppender.jTextPane = jTextPane;
		
	}
	
	/**
	 * Format and then append the loggingEvent to the stored JTextArea.
	 * This function will typically be called by log4j.
	 */
	@Override
	public void append(LoggingEvent loggingEvent) 
	{
		final String message = this.layout.format(loggingEvent);
		final Color color;
		
		if (loggingEvent.getLevel()==org.apache.log4j.Level.ERROR)
		{
			color= IbaColors.RED;//new Color(200,0,00);
		}
		else if (loggingEvent.getLevel()==org.apache.log4j.Level.DEBUG)
		{
			color= IbaColors.PURPLE;//new Color(00,150,00);
		}
		else if (loggingEvent.getLevel()==org.apache.log4j.Level.INFO)
		{
			color= IbaColors.GREEN;//new Color(00,150,00);
		}
		else if (loggingEvent.getLevel()==org.apache.log4j.Level.WARN)
		{
			color= IbaColors.ORANGE;//new Color(200,200,00);
		}
		else
			color=Color.BLACK;

		
		// Append formatted message to textarea using the Swing Thread.
		SwingUtilities.invokeLater(new Runnable() 
		{
			public void run() 
			{
				SimpleAttributeSet attributes = new SimpleAttributeSet();
				StyleConstants.setForeground( attributes, color );	
				
				if(jTextPane != null)
				{
					Document doc = jTextPane.getDocument();
										
					try 
				    {
						doc.insertString( doc.getLength(), message, attributes);
					} 
				    catch (BadLocationException e) 
				    {
				    	log.error("Error when displaying the a log4j message");
				    }
					
				}								
	           				
			}
		});
	}
}