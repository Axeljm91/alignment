/*
* Application : LookupAnalyser
* Filename : TagType.java
* Author : Henrotin Sebastien
* Date : 06/04/2010
* Company : IBA
* Version : 0.1
*/

package com.iba.device;

/**
 * The differents type of tags : integer, real, boolean, byte, double integer and other (like Lookup in the BCREU e.g.)
 * @author Henrotin Sebastien
 */
public enum TagType 
{
	BOOLEAN, //integer
	BYTE, //real
	DATE, //boolean
	DINT, //byte
	FLOAT, //double integer
	INT, //string
	INT16, //lookup table e.g.
	INT32,
	INT8,
	IPADDR,
	OTHER,
	STRING,
	TIME,
	UINT16,
	UINT32,
	UINT8,
	ARRAY, REAL, WORD,BOOL
}
