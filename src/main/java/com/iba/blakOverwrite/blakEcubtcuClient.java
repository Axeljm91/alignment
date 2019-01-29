package com.iba.blakOverwrite;


/////////////////////////////////////////////////////////////////////
//Copyright (c) by Ion Beam Applications S.A.
//All rights reserved
//
//Rue du cyclotron, 3
//B-1348 Louvain-la-Neuve
//Tel : +32.10.47.58.11
/////////////////////////////////////////////////////////////////////


import com.iba.blak.BlakConstants;
import com.iba.blak.BlakPreferences;
import com.iba.blak.common.PopupDisplayer;
import com.iba.blak.common.Utils;
import com.iba.blak.ecubtcu.BlakEcubtcu;
import com.iba.blak.ecubtcu.BlakRpcClientProxy;
import com.iba.blak.device.impl.EcubtcuCommandException;
import com.iba.blak.device.impl.EcubtcuNotConnectedException;
import com.iba.icomp.comm.rpc.RpcFileProgramFactory;
import com.iba.icomp.core.util.Logger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.acplt.oncrpc.OncRpcPortmapClient;
import org.acplt.oncrpc.OncRpcProtocols;
import org.acplt.oncrpc.OncRpcServerIdent;
import org.springframework.core.io.Resource;

/**
* Common ecubtcu RPC function call for all sites.
*/
public class blakEcubtcuClient extends BlakEcubtcu
{

private static final List<String> PREFIX_LIST = Arrays.asList("ECUBTCU", "ISEU", "ANALOG", "CANMAGNET",
      "BPM", "BS", "DEGRADER", "SLITS", "MODULARPS", "MOTORISEDSLITS");

private static final List<String> FUNCTION_LIST = Arrays.asList("ISEUREQUESTSETSINGLEPULSEMODE",
      "ANALOGSETCURRENT", "ANALOGSETVOLTAGE", "CANMAGNETSETCURRENT", "CANMAGNETSETDIGITAL",
      "CANMAGNETSETFIELD", "BPMINSERT", "BPMRETRACT", "BPMSTARTCONTINUOUSACQUISITION",
      "BPMSTOPCONTINUOUSACQUISITION", "BPMSTARTPROFILEACQUISITION", "BPMSTOPPROFILEACQUISITION",
      "BPMRESET", "BSINSERT", "BSRETRACT", "DEGRADERHIGHGOPOSITION", "DEGRADERGOSTAIR",
      "DEGRADERGOSPECIALBLOCK", "DEGRADERGOENERGY", "DEGRADERGOHOME", "SLITSGOHOME", "SLITSGOMM",
      "SLITSGOSTEP", "MODULARPSSETCURRENT", "ANALOGPSSETCURRENT", "ANALOGPSSETVOLTAGE",
      "MOTORISEDSLITSGOHOME", "MOTORISEDSLITSGOTOOPENING", "MOTORISEDSLITSGOTOPOSITION");

private static final int ISEUREQUESTSETSINGLEPULSEMODE = 0;
private static final int ANALOGSETCURRENT = 1;
private static final int ANALOGSETVOLTAGE = 2;
private static final int CANMAGNETSETCURRENT = 3;
private static final int CANMAGNETSETDIGITAL = 4;
private static final int CANMAGNETSETFIELD = 5;
private static final int BPMINSERT = 6;
private static final int BPMRETRACT = 7;
private static final int BPMSTARTCONTINUOUSACQUISITION = 8;
private static final int BPMSTOPCONTINUOUSACQUISITION = 9;
private static final int BPMSTARTPROFILEACQUISITION = 10;
private static final int BPMSTOPPROFILEACQUISITION = 11;
private static final int BPMRESET = 12;
private static final int BSINSERT = 13;
private static final int BSRETRACT = 14;
private static final int DEGRADERHIGHGOPOSITION = 15;
private static final int DEGRADERGOSTAIR = 16;
private static final int DEGRADERGOSPECIALBLOCK = 17;
private static final int DEGRADERGOENERGY = 18;
private static final int DEGRADERGOHOME = 19;
private static final int SLITSGOHOME = 20;
private static final int SLITSGOMM = 21;
private static final int SLITSGOSTEP = 22;
private static final int MODULARPSSETCURRENT = 23;
private static final int ANALOGPSSETCURRENT = 24;
private static final int ANALOGPSSETVOLTAGE = 25;
private static final int MOTORISEDSLITSGOHOME = 26;
private static final int MOTORISEDSLITSGOTOOPENING = 27;
private static final int MOTORISEDSLITSGOTOPOSITION = 28;
BlakRpcClientProxy mClient = new BlakRpcClientProxy();
RpcFileProgramFactory mFactory;

private String mEcubtcuAddress ;

public void setRpcProgrameFile(Resource pRes) throws Exception
{
   mFactory = new RpcFileProgramFactory();
   mFactory.setPrefixes(PREFIX_LIST);
   mFactory.setResource(pRes);
   mFactory.load();
}

public void setEcubtcuAddress(String strAddress)
{
	mEcubtcuAddress = strAddress ;
}

public String getEcubtcuAddress(String strAddress)
{
	return mEcubtcuAddress ;
}

@Override
public void connect()
{
   // if (!Blak.security.canConnectToEcubtcu())
   // {
   // Blak.security.ecubtcuConnectionRefusedMessage();
   // return;
   // }

//   String ecubtcuAddress = BlakPreferences.getCurrentSiteString(BlakConstants.NETWORK_ECUBTCU_ADDRESS);
   try
   {

//      if (ecubtcuAddress.equals(""))
 //     {
//         String msg = "No address specified for ECU/BTCU";
//         Logger.getLogger().warn(msg);
//         SwingUtilities.invokeLater(
//               new PopupDisplayer(msg, "ECU/BTCU connect", JOptionPane.WARNING_MESSAGE));
//         return;
//      }

//      InetAddress address = InetAddress.getByName(ecubtcuAddress);
      InetAddress address = InetAddress.getByName(mEcubtcuAddress);

      // Map ports and find out RPC number to use to communicate with ecubtcu
      OncRpcPortmapClient portmapClient = new OncRpcPortmapClient(address);
      OncRpcServerIdent[] serverList = portmapClient.listServers();

      // List servers in debug information
      Logger.getLogger().debug("Server list items :");
      for (int i = 0; i < serverList.length; ++i)
      {
         Logger.getLogger().debug(
               " - RPC Number = " + serverList[i].program + ", Version = " + serverList[i].version
                     + ", Protocol = " + (serverList[i].protocol == OncRpcProtocols.ONCRPC_TCP ? "TCP" :
                     "UDP") + ", Port = " + serverList[i].port);
      }

      // All items should have the same RPC number so we take the first one
      int rpcNumber = serverList[0].program;

//      mClient.setServerHost(ecubtcuAddress);
      mClient.setServerHost(mEcubtcuAddress);
      mClient.setServerPort(0);
      mClient.setProgram(rpcNumber);
      mClient.setVersion(1);
      mClient.setRpcProgramFactory(mFactory);
      mClient.initialize();
      mClient.connect();

      setConnected(true);

   }
   catch (UnknownHostException e)
   {
//      String msg = "ECU/BTCU host not found : " + ecubtcuAddress;
      String msg = "ECU/BTCU host not found : " + mEcubtcuAddress;
      Logger.getLogger().warn(msg);
      SwingUtilities.invokeLater(new PopupDisplayer(msg, "ECU/BTCU connect", JOptionPane.WARNING_MESSAGE));
   }
   catch (Exception e)
   {
      String msg = "Error connecting to ECU/BTCU : " + e.getMessage();
      Logger.getLogger().warn(msg);
      SwingUtilities.invokeLater(new PopupDisplayer(msg, "ECU/BTCU connect", JOptionPane.WARNING_MESSAGE));
   }

}

@Override
public void disconnect()
{
   try
   {
      mClient.disconnect();
   }
   catch (Exception e)
   {
      String msg = "Error connecting to ECU/BTCU : " + e.getMessage();
      Logger.getLogger().warn(msg);
      SwingUtilities.invokeLater(
            new PopupDisplayer(msg, "ECU/BTCU disconnect", JOptionPane.WARNING_MESSAGE));
   }

   setConnected(false);

}

@Override
public void iseuRequestSetSinglePulseMode(boolean single) throws EcubtcuCommandException,
      EcubtcuNotConnectedException
{
   Object[] param = new Object[]{single ? Integer.valueOf(1) : Integer.valueOf(0)};
   String function = FUNCTION_LIST.get(ISEUREQUESTSETSINGLEPULSEMODE);
   String prefix = PREFIX_LIST.get(1);
   if (mClient.hasFuntion(prefix, function))
   {
      mClient.makeRpcCall(prefix, function, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
   }
   else
   {
      throw new EcubtcuCommandException(Utils.getCurrentFunction());
   }

}

@Override
public void analogSetCurrent(String name, double current) throws EcubtcuCommandException,
      EcubtcuNotConnectedException
{
   Object[] param = new Object[]{name, Double.valueOf(current)};

   String function = FUNCTION_LIST.get(ANALOGSETCURRENT);
   String prefix = PREFIX_LIST.get(2);
   if (mClient.hasFuntion(prefix, function))
   {
      mClient.makeRpcCall(prefix, function, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
   }
   else
   {
      function = FUNCTION_LIST.get(ANALOGPSSETCURRENT);
      if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
      {
         mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
      }
      else
      {
         throw new EcubtcuCommandException(Utils.getCurrentFunction());
      }
   }
}

@Override
public void modularPsSetCurrent(String name, double current) throws EcubtcuCommandException,
      EcubtcuNotConnectedException
{
   Object[] param = new Object[]{name, Double.valueOf(current)};

   String function = FUNCTION_LIST.get(MODULARPSSETCURRENT);
   String prefix = PREFIX_LIST.get(8);

   if (mClient.hasFuntion(prefix, function))
   {
      mClient.makeRpcCall(prefix, function, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
   }
   else
   {
      throw new EcubtcuCommandException(Utils.getCurrentFunction());
   }

}

@Override
public void analogSetVoltage(String name, double voltage) throws EcubtcuCommandException,
      EcubtcuNotConnectedException
{

   Object[] param = new Object[]{name, Double.valueOf(voltage)};
   String function = FUNCTION_LIST.get(ANALOGSETVOLTAGE);
   String prefix = PREFIX_LIST.get(2);
   if (mClient.hasFuntion(prefix, function))
   {
      mClient.makeRpcCall(prefix, function, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
   }
   else
   {
      function = FUNCTION_LIST.get(ANALOGPSSETVOLTAGE);
      if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
      {
         mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
      }
      else
      {
         throw new EcubtcuCommandException(Utils.getCurrentFunction());
      }
   }
}

@Override
public void canMagnetSetCurrent(String name, double current) throws EcubtcuCommandException,
      EcubtcuNotConnectedException
{
   Object[] param = new Object[]{name, Double.valueOf(current)};

   String function = FUNCTION_LIST.get(CANMAGNETSETCURRENT);
   String prefix = PREFIX_LIST.get(3);
   if (mClient.hasFuntion(prefix, function))
   {
      mClient.makeRpcCall(prefix, function, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
   }
   else
   {
      throw new EcubtcuCommandException(Utils.getCurrentFunction());
   }
}

@Override
public void canMagnetSetDigital(String name, double digital) throws EcubtcuCommandException,
      EcubtcuNotConnectedException
{
   Object[] param = new Object[]{name, Double.valueOf(digital)};

   String function = FUNCTION_LIST.get(CANMAGNETSETDIGITAL);
   String prefix = PREFIX_LIST.get(3);
   if (mClient.hasFuntion(prefix, function))
   {
      mClient.makeRpcCall(prefix, function, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
   }
   else
   {
      throw new EcubtcuCommandException(Utils.getCurrentFunction());
   }
}

@Override
public void canMagnetSetField(String name, double field) throws EcubtcuCommandException,
      EcubtcuNotConnectedException
{
   Object[] param = new Object[]{name, Double.valueOf(field)};

   String function = FUNCTION_LIST.get(CANMAGNETSETFIELD);
   String prefix = PREFIX_LIST.get(3);
   if (mClient.hasFuntion(prefix, function))
   {
      mClient.makeRpcCall(prefix, function, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
   }
   else
   {
      throw new EcubtcuCommandException(Utils.getCurrentFunction());
   }
}

@Override
public void bpmInsert(String name) throws EcubtcuCommandException, EcubtcuNotConnectedException
{

   Object[] param = new Object[]{name};

   String function = FUNCTION_LIST.get(BPMINSERT);
   String prefix = PREFIX_LIST.get(4);
   if (mClient.hasFuntion(prefix, function))
   {
      mClient.makeRpcCall(prefix, function, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
   }
   else
   {
      throw new EcubtcuCommandException(Utils.getCurrentFunction());
   }

}

@Override
public void bpmRetract(String name) throws EcubtcuCommandException, EcubtcuNotConnectedException
{
   Object[] param = new Object[]{name};

   String function = FUNCTION_LIST.get(BPMRETRACT);
   String prefix = PREFIX_LIST.get(4);
   if (mClient.hasFuntion(prefix, function))
   {
      mClient.makeRpcCall(prefix, function, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
   }
   else
   {
      throw new EcubtcuCommandException(Utils.getCurrentFunction());
   }

}

@Override
public void bpmStartContinuousAcquisition(String name) throws EcubtcuCommandException,
      EcubtcuNotConnectedException
{

   Object[] param = new Object[]{name};

   String function = FUNCTION_LIST.get(BPMSTARTCONTINUOUSACQUISITION);
   String prefix = PREFIX_LIST.get(4);
   if (mClient.hasFuntion(prefix, function))
   {
      mClient.makeRpcCall(prefix, function, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
   }
   else
   {
      throw new EcubtcuCommandException(Utils.getCurrentFunction());
   }

}

@Override
public void bpmStopContinuousAcquisition(String name) throws EcubtcuCommandException,
      EcubtcuNotConnectedException
{

   Object[] param = new Object[]{name};

   String function = FUNCTION_LIST.get(BPMSTOPCONTINUOUSACQUISITION);
   String prefix = PREFIX_LIST.get(4);
   if (mClient.hasFuntion(prefix, function))
   {
      mClient.makeRpcCall(prefix, function, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
   }
   else
   {
      throw new EcubtcuCommandException(Utils.getCurrentFunction());
   }

}

@Override
public void bpmStartProfileAcquisition(String name) throws EcubtcuCommandException,
      EcubtcuNotConnectedException
{

   Object[] param = new Object[]{name, new Integer(1)};

   String function = FUNCTION_LIST.get(BPMSTARTPROFILEACQUISITION);
   String prefix = PREFIX_LIST.get(4);
   if (mClient.hasFuntion(prefix, function))
   {
      mClient.makeRpcCall(prefix, function, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
   }
   else
   {
      throw new EcubtcuCommandException(Utils.getCurrentFunction());
   }

}

@Override
public void bpmStopProfileAcquisition(String name) throws EcubtcuCommandException,
      EcubtcuNotConnectedException
{

   Object[] param = new Object[]{name};

   String function = FUNCTION_LIST.get(BPMSTOPPROFILEACQUISITION);
   String prefix = PREFIX_LIST.get(4);
   if (mClient.hasFuntion(prefix, function))
   {
      mClient.makeRpcCall(prefix, function, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
   }
   else
   {
      throw new EcubtcuCommandException(Utils.getCurrentFunction());
   }

}

@Override
public void bpmReset(String name) throws EcubtcuCommandException, EcubtcuNotConnectedException
{

   Object[] param = new Object[]{name};

   String function = FUNCTION_LIST.get(BPMRESET);
   String prefix = PREFIX_LIST.get(4);
   if (mClient.hasFuntion(prefix, function))
   {
      mClient.makeRpcCall(prefix, function, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
   }
   else
   {
      throw new EcubtcuCommandException(Utils.getCurrentFunction());
   }

}

@Override
public void bsInsert(String name) throws EcubtcuCommandException, EcubtcuNotConnectedException
{
   Object[] param = new Object[]{name};

   String function = FUNCTION_LIST.get(BSINSERT);
   String prefix = PREFIX_LIST.get(5);
   if (mClient.hasFuntion(prefix, function))
   {
      mClient.makeRpcCall(prefix, function, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
   }
   else
   {
      throw new EcubtcuCommandException(Utils.getCurrentFunction());
   }

}

@Override
public void bsRetract(String name) throws EcubtcuCommandException, EcubtcuNotConnectedException
{

   Object[] param = new Object[]{name};

   String function = FUNCTION_LIST.get(BSRETRACT);
   String prefix = PREFIX_LIST.get(5);
   if (mClient.hasFuntion(prefix, function))
   {
      mClient.makeRpcCall(prefix, function, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
   }
   else
   {
      throw new EcubtcuCommandException(Utils.getCurrentFunction());
   }
}

@Override
public void degraderHighGoPosition(int motorStep) throws EcubtcuCommandException,
      EcubtcuNotConnectedException
{

   Object[] param = new Object[]{Integer.valueOf(motorStep)};

   String function = FUNCTION_LIST.get(DEGRADERHIGHGOPOSITION);
   String prefix = PREFIX_LIST.get(6);
   if (mClient.hasFuntion(prefix, function))
   {
      mClient.makeRpcCall(prefix, function, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
   }
   else
   {
      throw new EcubtcuCommandException(Utils.getCurrentFunction());
   }
}

@Override
public void degraderGoStair(int stairIndex) throws EcubtcuCommandException, EcubtcuNotConnectedException
{

   Object[] param = new Object[]{Integer.valueOf(stairIndex)};

   String function = FUNCTION_LIST.get(DEGRADERGOSTAIR);
   String prefix = PREFIX_LIST.get(6);
   if (mClient.hasFuntion(prefix, function))
   {
      mClient.makeRpcCall(prefix, function, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
   }
   else
   {
      throw new EcubtcuCommandException(Utils.getCurrentFunction());
   }
}

@Override
public void degraderGoSpecialBlock(int blockId) throws EcubtcuCommandException,
      EcubtcuNotConnectedException
{

   Object[] param = new Object[]{Integer.valueOf(blockId)};

   String function = FUNCTION_LIST.get(DEGRADERGOSPECIALBLOCK);
   String prefix = PREFIX_LIST.get(6);
   if (mClient.hasFuntion(prefix, function))
   {
      mClient.makeRpcCall(prefix, function, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
   }
   else
   {
      throw new EcubtcuCommandException(Utils.getCurrentFunction());
   }

}

@Override
public void degraderGoEnergy(double energy) throws EcubtcuCommandException, EcubtcuNotConnectedException
{

   Object[] param = new Object[]{Double.valueOf(energy)};

   String function = FUNCTION_LIST.get(DEGRADERGOENERGY);
   String prefix = PREFIX_LIST.get(6);
   if (mClient.hasFuntion(prefix, function))
   {
      mClient.makeRpcCall(prefix, function, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
   }
   else
   {
      throw new EcubtcuCommandException(Utils.getCurrentFunction());
   }

}

@Override
public void degraderGoHome() throws EcubtcuCommandException, EcubtcuNotConnectedException
{
   Object[] param = new Object[]{};

   String function = FUNCTION_LIST.get(DEGRADERGOHOME);
   String prefix = PREFIX_LIST.get(6);
   if (mClient.hasFuntion(prefix, function))
   {
      mClient.makeRpcCall(prefix, function, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
   }
   else
   {
      throw new EcubtcuCommandException(Utils.getCurrentFunction());
   }
}

@Override
public void slitsGoHome(String name) throws EcubtcuCommandException, EcubtcuNotConnectedException
{
   Object[] param = new Object[]{};

   String function = FUNCTION_LIST.get(SLITSGOHOME);
   String prefix = PREFIX_LIST.get(7);
   String motorisedFunction = FUNCTION_LIST.get(MOTORISEDSLITSGOHOME);
   String motorisedPrefix = PREFIX_LIST.get(9);
   if (mClient.hasFuntion(prefix, function))
   {
      mClient.makeRpcCall(prefix, function, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
   }
   else if (mClient.hasFuntion(motorisedPrefix, motorisedFunction))
   {
      mClient.makeRpcCall(motorisedPrefix, motorisedFunction, param);
   }
   else
   {
      throw new EcubtcuCommandException(Utils.getCurrentFunction());
   }
}

@Override
public void slitsGoMm(String name, double pos) throws EcubtcuCommandException, EcubtcuNotConnectedException
{

   Object[] param = new Object[]{name, Double.valueOf(pos)};

   String function = FUNCTION_LIST.get(SLITSGOMM);
   String prefix = PREFIX_LIST.get(7);
   String motorisedFunction = FUNCTION_LIST.get(MOTORISEDSLITSGOTOOPENING);
   String motorisedPrefix = PREFIX_LIST.get(9);
   if (mClient.hasFuntion(prefix, function))
   {
      mClient.makeRpcCall(prefix, function, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
   }
   else if (mClient.hasFuntion(motorisedPrefix, motorisedFunction))
   {
      mClient.makeRpcCall(motorisedPrefix, motorisedFunction, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + motorisedFunction))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + motorisedFunction, param);
   }
   else
   {
      throw new EcubtcuCommandException(Utils.getCurrentFunction());
   }
}

@Override
public void slitsGoStep(String name, int step) throws EcubtcuCommandException, EcubtcuNotConnectedException
{
   Object[] param = new Object[]{name, Integer.valueOf(step)};

   String function = FUNCTION_LIST.get(SLITSGOSTEP);
   String prefix = PREFIX_LIST.get(7);
   String motorisedFunction = FUNCTION_LIST.get(MOTORISEDSLITSGOTOPOSITION);
   String motorisedPrefix = PREFIX_LIST.get(9);
   if (mClient.hasFuntion(prefix, function))
   {
      mClient.makeRpcCall(prefix, function, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + function, param);
   }
   else if (mClient.hasFuntion(motorisedPrefix, motorisedFunction))
   {
      mClient.makeRpcCall(motorisedPrefix, motorisedFunction, param);
   }
   else if (mClient.hasFuntion(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + motorisedFunction))
   {
      mClient.makeRpcCall(PREFIX_LIST.get(0), PREFIX_LIST.get(0) + motorisedFunction, param);
   }
   else
   {
      throw new EcubtcuCommandException(Utils.getCurrentFunction());
   }

}

}
