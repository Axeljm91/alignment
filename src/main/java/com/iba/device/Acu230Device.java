/*
* Application : LookupAnalyser
* Filename : Acu230Device.java
* Author : Henrotin S�bastien
* Date : 06/04/2010
* Company : IBA
* Version : 0.1
*/

package com.iba.device;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import java.nio.ByteBuffer;


/**
 * This class represent a connector, able to connect to a certain ACU. Use this class to get the value of a tag. 
 * This class takes care of the creation of the transmitted frame and of the parsing of the answer.
 * @author S�bastien Henrotin
 */
public class Acu230Device extends Device
{
	/**
	 * String used in the xml file (attribute for the index of the bit)
	 */
	public static final String ACU_BITNBR = "bitNbr"; 

	/**
	 * String used in the xml file (attribute for the dbnr)
	 */
	public static final String ACU_DBNR = "dbnr";

	/**
	 * String used in the xml file (attribute for the start adress)
	 */
	public static final String ACU_START_ADRESS = "startAdress";

	/**
	 * The logger. See http://logging.apache.org/log4j/1.2/index.html for more information.
	 */
	private static Logger log=Logger.getLogger(new Throwable().getStackTrace()[0].getClassName());

	/**
	 * Timeout after which we consider that the device is not answering, when we establish the communication
	 */
	private static final int TIMEOUT=10000;

	/**
	 * Convert a float from the ACU format. (2 bytes)
	 * @param arr the array of bytes coming from the ACU
	 * @param start the index of the array where we should begin to look for the float.
	 * @return the float
	 */
	public static float arr2float (byte[] arr, int start,boolean inversion) 
	{
		int i = 0;
		int len = 4;
		int cnt = 0;
		byte[] tmp = new byte[len];
	
		if (!inversion)
		{
			for (i = start; i < (start + len); i++) 
			{
				tmp[cnt] = arr[i];
				cnt++;
			}
		}
		else
		{
			for (i = start+len-1; i >= start; i--)
			{
				tmp[cnt] = arr[i];
				cnt++;
			}			
		}
		
		int accum = 0;
		i = 0;
		for ( int shiftBy = 0; shiftBy < 32; shiftBy += 8 ) 
		{
			accum |= ( (long)( tmp[i] & 0xff ) ) << shiftBy;
			i++;
		}
		return Float.intBitsToFloat(accum); 
	}
	
	/**
	 * Convert a int from the ACU format. (2 bytes)
	 * @param arr the array of bytes coming from the ACU
	 * @param start the index of the array where we should begin to look for the int.
	 * @return the int
	 */
	private static int arr2integer(byte[] arr,int start)
	{		
		int result = (arr[start]&0xFF)*256;
		result+=(arr[start+1]&0xFF);

		//Conversion -unsigned->signed
		if (result>=32768)
			result=result-65536;
		
		return result;
	}
	
	/**
	 * Convert a long from the ACU format. (4 bytes)
	 * @param arr the array of bytes coming from the ACU
	 * @param start the index of the array where we should begin to look for the long.
	 * @return the long
	 */
	private static long arr2long(byte[] arr,int start)
	{		
		long result = (long)((arr[start]&0xFF)*Math.pow(2, 24));
		result+=(long)((arr[start+1]&0xFF)*Math.pow(2, 16));
		result+=(long)((arr[start+2]&0xFF)*Math.pow(2, 8));
		result+=((arr[start+3]&0xFF));
		
		return result;
	}

	/**
	 * Stream used to write data to the PLC (data going from the PLC to the server)
	 */
	private InputStream commandInputStream;

	/**
	 * Stream used to write data to the PLC (data going from the server to the PLC)
	 */
	private OutputStream commandOutputStream;

	/**
	 * Socket used to write data to the physical device
	 */
	private Socket commandSocket;
	
	/**
	 * Stream used for the communication with the PLC (data going from the PLC to the server)
	 */
	private InputStream infoInputStream;
	
	/**
	 * Stream used to read data from the PLC (data going from the server to the PLC)
	 */
	private OutputStream infoOutputStream;
	
	/**
	 * Socket used to read data on the physical device
	 */
	private Socket infoSocket;
	
	/**
	 * Constructor
	 * 
	 * @param site The site where is located the ACU
	 * @param name The name of the device
	 * @param ip The ip adress in the format XXX.XXX.XXX.XXX
	 * @param info In that case, info is actually the port on which the ACU is listening (generally 2002)
	 */
	public Acu230Device(Site site, String name, String ip, String info,DeviceType deviceType) 
	{
		super(site, name, ip, info,deviceType);
		log.info("AcuDevice created");
	}
	
	/**
	 * Convert a boolean from the ACU format. (2 bytes)
	 * @param answer the array of bytes coming from the ACU
	 * @param start the index of the array where we should begin to look for the boolean.
	 * @param bitNbr the index of the bit in the byte/
	 * @return the boolean in a double format
	 */
	private boolean arr2bool(byte[] answer, int start, int bitNbr)
	{
		byte theByte=answer[start];
		double result=0;
		for (int j=0;j<bitNbr;j++)
		{
	        theByte =(byte) (theByte >> 1&0xFF);
		}
		result= theByte & 0x01;
		
		return (result==1)?true:false;
	}

	/**
	 * Get a specific byte from an array of bytes and convert it into a double
	 * @param answer the array of bytes
	 * @param start the index of the byte you want to get.
	 * @return the byte converted into a double
	 */
	private byte arr2byte(byte[] answer, int start) 
	{
		return answer[start];
	}

	/**
	 * Establish a connection with the ACU. If you are already connected, you get a warning. 
	 */
	@Override
	public synchronized void connect()
	{
		if (!connected)
		{
			try 
			{
				log.info("Trying to connect to acu :"+this);
				infoSocket = new Socket();
				commandSocket=new Socket();
				
				try 
				{
					infoSocket.bind(null);
					commandSocket.bind(null);
				} 
				catch (IOException e) 
				{
					log.error("Impossible to make the binding of the port for :"+this);
					disconnect();
					return;
				}
				
				infoSocket.connect(new InetSocketAddress(ip, Integer.parseInt(infos[0])), TIMEOUT);
				infoInputStream=infoSocket.getInputStream();
				infoOutputStream=infoSocket.getOutputStream();
				
				commandSocket.connect(new InetSocketAddress(ip, Integer.parseInt(infos[1])), TIMEOUT);
				commandInputStream=commandSocket.getInputStream();
				commandOutputStream=commandSocket.getOutputStream();

			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				log.error("Error during the connection :"+this+", "+e.getMessage());
				disconnect();
				return;
			}
			connected=true;
			log.info("ACU230Device connected : "+this);
		}
		else
			log.warn("ACU230Device was already connected: "+this);
	}

	/**
	 * Disconnect from the ACU. If you are not connected, you get a warning.
	 */
	@Override
	public synchronized void disconnect()
	{
		if (connected)
		{
			try
			{
				log.info("Trying to disconnect from acu :"+this);

				if (infoInputStream!=null)
					infoInputStream.close();
				
				if(infoOutputStream!=null)
					infoOutputStream.close();
				
				if (infoSocket!=null)
					infoSocket.close();

				if (commandInputStream!=null)
					commandInputStream.close();
				
				if (commandOutputStream!=null)
					commandOutputStream.close();			
				
				if (commandSocket!=null)
					commandSocket.close();
			}
			catch (IOException e) 
			{
				log.error("Error during the disconnection :"+this);
				connected=false;
				return;
			}
			connected=false;
			log.info("ACU230Device disconnected: "+this);
		}
		else
			log.warn("ACU230Device was already disconnected: "+this);
	}
	
	private byte[] doubleToS7Real(double value) 
	{
		byte[] result=new byte[4];
		
//		double f, tmp;
//		int i, expo, mant, signe;
//
//		if (value<0)
//		{
//			value = value*-1;
//			signe = 0x80;
//		}
//		else
//			signe =0x00;
//
//		expo = (int)Math.floor(Math.log(value)/Math.log(2))+127;
//
//		f=Math.pow(2,Math.log(value)/Math.log(2)+127-expo)-1;
//
//		mant=0;
//		tmp=f;
//		for(i=1;i<=16;i++)
//		{
//			if (tmp/Math.pow(2,-i)>=1.0)
//			{
//				mant = mant+(0x01<<(16-i));
//				tmp = tmp-Math.pow(2,-i);
//			}
//		}
//		result[0] = (byte)((expo>>1)&0x7F|signe);
//		result[1] = (byte)((((mant>>1)&(0xFF00))>>8)+(expo&0x01)*0x80);
//		result[2] = (byte)((mant>>1)&(0x00FF));
//		result[3] = 0;

		ByteBuffer.wrap(result).putFloat((float)value);

		return result;
	}

	/**
	 * Generate the standard request. That needs to appear at the beginning of any frame when you request the value of a tag.
	 * @return the first 15 bytes of any frame
	 */
	private byte[] generateStandardRequest(boolean write,int maxSize)
	{
		byte[] request=new byte[maxSize];
		request[0]='S' & 0xFF;
		request[1]='5' & 0xFF;
		request[2]=16 & 0xFF;    //                      SIEMENS PROTOCOL
		request[3]=1 & 0xFF;     //                      SIEMENS PROTOCOL
		request[4]=3 & 0xFF;     //                      SIEMENS PROTOCOL
		
		if (write)
			request[5]=3 & 0xFF;     //                      SIEMENS PROTOCOL
		else
			request[5]=5 & 0xFF;     //                      SIEMENS PROTOCOL

		request[6]=3 & 0xFF;     //                      SIEMENS PROTOCOL
		request[7]=8 & 0xFF;     //                      SIEMENS PROTOCOL
		request[8]=1 & 0xFF;     //ORG ID                IBA PROTOCOL
		request[12]=00 & 0xFF;   //NBR of ..             IBA PROTOCOL
		request[14]=(byte)(255 & 0xFF);  //                      SIEMENS PROTOCOL
		request[15]=2 & 0xFF;    //                      SIEMENS PROTOCOL
		
		return request;
	}

	/**
	 * Generate a request to the acu to retreive the value of the specified tag
	 * @param tag The tag you want to read
	 * @return the value. It returns null if the request didn't succeed.
	 */
	@Override
	public synchronized Object getTagValue(Tag tag)
	{		
		TagGroupType type=tag.getGroup().getType();
		if (!(type.equals(TagGroupType.READ)||type.equals(TagGroupType.READWRITE)))
		{
			log.warn("Trying to read a tag and its group is not from the type READ nor READWRITE");
			return null;
		}
		
		if (!isConnected())
		{
			log.warn("Trying to read a value on an ACU but the device is not connected");
			return null;
		}

		//Generate the header of the request
		byte[] request=generateStandardRequest(false,16);
		
	//	System.out.println(printByteMessage(request));
		
		//These parameters : dbnr, startAdress and bit index are stored in the tag objects.
		int dbnr=(Integer)tag.getParameter(ACU_DBNR);
		int startAdress=(Integer)tag.getParameter(ACU_START_ADRESS);
		
		//We divide the adress in two bytes
		int startAdressH=(startAdress/256);
		int startAdressL=(startAdress%256);
		
		request[9]=(byte)(dbnr & 0xFF);
		request[10]=(byte)(startAdressH & 0xFF);
		request[11]=(byte)(startAdressL & 0xFF);
		
		if (tag.getType().equals(TagType.BOOLEAN)||tag.getType().equals(TagType.BYTE))
			request[13]=(byte)(1 & 0xFF);
		else
			request[13]=(byte)(2 & 0xFF);
			
		try 
		{
			if (infoOutputStream!=null)
				infoOutputStream.write(request,0,16);
			else
			{
				log.error("The connection with ACU : "+this+" is not working");
				disconnect();
				return null;
			}
		} 
		catch (IOException e) 
		{
			log.error("Error when sending a request for the tag : "+tag+" from acu :"+this.name+" of site :"+this.getSite().getName());
			disconnect();
			return null;
		}
		
		byte[] answer=new byte[20];
		try 
		{
			if (infoInputStream!=null)
				infoInputStream.read(answer,0,20);
			else
			{
				log.error("The connection with ACU : "+this+" is not working");
				disconnect();
				return null;
			}
		} 
		catch (IOException e) 
		{
			log.error("Error when reading the answer for the tag : "+tag+" from acu :"+this.name+" from site :"+this.getSite().getName());
			disconnect();
			return null;
		}
		
		//Different treatment depending of the type of tag
		Object result;
		if (tag.getType()==TagType.INT)
		{
			result=(int)arr2integer(answer, 16);
		}
		else if (tag.getType()==TagType.FLOAT)
		{
			result=(double)arr2float(answer, 16,true);
		}
		else if (tag.getType()==TagType.BOOLEAN)
		{
			int bitNbr=(Integer)tag.getParameter(ACU_BITNBR);
			result=(boolean)arr2bool(answer,16,bitNbr);
		}
		else if (tag.getType()==TagType.BYTE)
		{
			result=(byte)arr2byte(answer,16);
		}
		else if (tag.getType()==TagType.DINT)
		{
			result=(long)arr2long(answer, 16);
		}		
		else
		{
			log.error("The type of the tag : "+tag+" is not known");
			return null;
		}
		//log.info("Read value for tag: "+tag+" on ACU: "+this.name+" from site: "+this.getSite().getName()+": "+result);
		
		return result;
	}

	private byte[] intToS7Int(Integer value) 
	{
		byte[] result=new byte[2];
		result[0]=(byte)((value>>16)&0xFF);
		result[1]=(byte)(value&0xFF);
		return result;
	}

	/**
	 * Utility function to generate a message from the array of bytes coming or going from/to the ACU. Useful for debugging purpose.
	 * @param message the array you want to print
	 * @return the message as a String
	 */
	private String printByteMessage(byte[] message) 
	{
		StringBuffer result=new StringBuffer();
		for (int i=0;i<message.length;i++)
		{
			result.append(message[i]&0xFF);
			if (i!=message.length-1)
				result.append(" ");
		}
		return result.toString();
	}

	/**
	 * This function is not implemented for the ACU. That's here that we should add the code to allow to send a command to the ACU
	 */
	@Override
	public void setTagValue(Tag tag, Object value) 
	{
		TagGroupType type=tag.getGroup().getType();
		if (!(type.equals(TagGroupType.WRITE)||type.equals(TagGroupType.READWRITE)))
		{
			log.warn("Trying to write a tag and its group is not from the type WRITE nor READWRITE");
			return;
		}

		if (!isConnected())
		{
			log.warn("Trying to write a value on an ACU but the device is not connected");
			return;
		}
		
		byte[] request=generateStandardRequest(true, 20);
		
		//These parameters : dbnr, startAdress and bit index are stored in the tag objects.
		int dbnr=(Integer)tag.getParameter(ACU_DBNR);
		int startAdress=(Integer)tag.getParameter(ACU_START_ADRESS);
		
		//We divide the adress in two bytes
		int startAdressH=(startAdress/256);
		int startAdressL=(startAdress%256);
		
		request[9]=(byte)(dbnr & 0xFF);
		request[10]=(byte)(startAdressH & 0xFF);
		request[11]=(byte)(startAdressL & 0xFF);
		
		request[13]=2&0xFF;

		if (tag.getType()==TagType.FLOAT)
		{
			byte[] valueS7Real=doubleToS7Real((Double)value);
			for (int i=0;i<4;i++)
			{
				request[16+i]=(byte)(valueS7Real[i]&0xFF);
			}			
		}
		else if (tag.getType()==TagType.INT)
		{
			byte[] valueS7Int=intToS7Int((Integer)value);
			for (int i=0;i<2;i++)
			{
				request[16+i]=(byte)(valueS7Int[i]&0xFF);
			}
		}
				
		try 
		{
			if (commandOutputStream!=null)
			{
				commandOutputStream.write(request,0,20);
				commandOutputStream.flush();
				byte[] answer=new byte[255];
				commandInputStream.read(answer);
			}
			else
			{
				log.error("The connection with ACU : "+this+" is not working");
				disconnect();
				return;
			}
		} 
		catch (IOException e) 
		{
			log.error("Error when writing the value for the tag : "+tag+" from acu :"+this.name+" of site :"+this.getSite().getName());
			disconnect();
			return;
		}
	}
}
