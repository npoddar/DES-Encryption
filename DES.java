import java.io.*;
import java.lang.*;


public class DES {

		private long data = 0;
		private long key = 0;
		private long leftHalf; //left half of data after it has undergone initial perm
		private long rightHalf;  //right half of data after it has undergone initial perm
		
		private long keyPlus = 0;  //is K+ as described in Orlin grabber article.//
		private long keyPlusRight = 0;
		private long keyPlusLeft = 0;
		
		
		private long[][] key16 = new long[16][2];
		private long[] key16Comb = new long[16];
		private long[] key16Final = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		private int[] keyIteration = {1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1};
		
		private long R16L16 = 0;
		
		
		private long dataInitPerm = 0; //long holding the initial permutation of data 64-bits long
		//initial permutation table of data//
		private static int[] IP = {58, 50, 42, 34, 26, 18, 10, 2, 60, 52, 44, 36, 28, 20, 12, 4, 62, 54, 46, 38, 30, 22, 14, 6, 64, 56, 48, 40, 32, 24, 16, 8, 57, 49, 41, 33, 25, 17, 9, 1, 59, 51, 43, 35, 27, 19, 11, 3, 61, 53, 45, 37, 29, 21, 13, 5, 63, 55, 47, 39, 31, 23, 15, 7} ;
		private long[][] dataArray = new long[16][2];
		
		private long dataEncrypted = 0;
		
		private static int[] E = {32, 1, 2, 3, 4, 5, 4, 5, 6, 7, 8, 9, 8, 9, 10, 11, 12, 13, 12, 13, 14, 15, 16, 17, 16, 17, 18, 19, 20, 21, 20, 21, 22, 23, 24, 25, 24, 25, 26, 27, 28, 29, 28, 29, 30, 31, 32, 1} ;
		
		// Substitution lookup box//
		private static long[][][] S = { 
			{ {14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7}, {0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8}, {4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0}, {15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13} },
			{ {15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10}, {3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5}, {0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15}, {13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9} },
			{ {10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8}, {13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1}, {13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7}, {1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12} },
			{ {7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15}, {13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9}, {10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4}, {3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14} },
			{ {2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9}, {14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6}, {4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14}, {11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3} },
			{ {12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11}, {10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8}, {9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6}, {4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13} },
			{ {4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1}, {13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6}, {1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2}, {6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12} },
			{ {13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7}, {1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2}, {7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8}, {2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11} }
		};
		
		//P is the final permutation after substitution box lookup. Its 32-bit long//
		private static int[] P = {16, 7, 20, 21, 29, 12, 28, 17, 1, 15, 23, 26, 5, 18, 31, 10, 2, 8, 24, 14, 32, 27, 3, 9, 19, 13, 30, 6, 22, 11, 4, 25};
		
		//mask for a 64 bit long used for setting bit 1, bit 2, bit 3 ...
		
		//PC-1 as defined in the DES algorithm//
		private static int[] PC1 = {57, 49, 41, 33, 25, 17, 9, 1, 58, 50, 42, 34, 26, 18, 10, 2, 59, 51, 43, 35, 27, 19, 11, 3, 60, 52, 44, 36, 63, 55, 47, 39, 31, 23, 15, 7, 62, 54, 46, 38, 30, 22, 14, 6, 61, 53, 45, 37, 29, 21, 13, 5, 28, 20, 12, 4};
		
		private static int[] PC2 = {14, 17, 11, 24, 1, 5, 3, 28, 15, 6, 21, 10, 23, 19, 12, 4, 26, 8, 16, 7, 27, 20, 13, 2, 41, 52, 31, 37, 47, 55, 30, 40, 51, 45, 33, 48, 44, 49, 39, 56, 34, 53, 46, 42, 50, 36, 29, 32};
		
		//IP1 is IP^-1 as defined in the DES algorithm
		private static int[] IP1 = {40, 8, 48, 16, 56, 24, 64, 32, 39, 7, 47, 15, 55, 23, 63, 31, 38, 6, 46, 14, 54, 22, 62, 30, 37, 5, 45, 13, 53, 21, 61, 29, 36, 4, 44, 12, 52, 20, 60, 28, 35, 3, 43, 11, 51, 19, 59, 27, 34, 2, 42, 10, 50, 18, 58, 26, 33, 1, 41, 9, 49, 17, 57, 25};
	
		public DES(long indata, long inkey){
			data = indata;
			key = inkey;
		}
		
				
		//permuted key K+
		public void keyPlus(){
			for(int i= 0; i<56; i++){
				 keyPlus = 	( (keyPlus << 1) | ( ( (key << (PC1[i] - 1) ) & 0x8000000000000000L) >>> 63 ) );				
			}
		}
		
		
		public void keyPlusRight(){
				keyPlusRight = (keyPlus & 0x000000000fffffffL) ; //28 bit right key stored at lower end of long keyPlusRight
		}
		
		public void keyPlusLeft(){
			keyPlusLeft = (keyPlus & 0x00fffffff0000000L) >>> 28;  //28 bit left key stored at lower end of long keyPlusLeft
		}
		
		//generates the 16 pairs of keys
		public void keyGenerate(){
			long mask1 = 0x0000000008000000L;//reading in bit at position 28 and zeroing everything else
			long mask2 = 0x000000000c000000L;//reading in bits at position 28 and 27, and zeroing everything else
			long lower28 = 0xfffffff; //take the lower 28 bits. after a left shift, the two most significant bits still remain that needs to be discarded. anding with this variable gives us only the lower 28 bits, which is what we need
			key16[0][0] = ( ( (keyPlusLeft << 1) | ( (keyPlusLeft & mask1) >>> 27) ) & lower28); 			//0 is left, 1 is right in key16 array
			key16[0][1] = ( ( (keyPlusRight << 1) | ((keyPlusRight & mask1) >>> 27) ) & lower28);			//0 is left, 1 is right in key16 array
			
			for(int i= 1; i < 16; i++)
			{
				if(keyIteration[i] == 1){
					key16[i][0] = ( ( (key16[i-1][0] << 1) | ( (key16[i-1][0] & mask1) >>> 27) ) & lower28) ;
					key16[i][1] = ( ( (key16[i-1][1] << 1) | ( (key16[i-1][1] & mask1) >>> 27) ) & lower28)  ;
				}
				
				else{
					key16[i][0] = ( ( (key16[i-1][0] << 2) | ( (key16[i-1][0] & mask2) >>> 26) ) & lower28) ;
					key16[i][1] = ( ( (key16[i-1][1] << 2) | ( (key16[i-1][1] & mask2) >>> 26) ) & lower28) ;
				}
				
			}
			
		}
		
		//combine the left and right halves of the 16 keys generated in keyGenerate() into a 56 bit long key
		public void keyCombine(){
			for(int i = 0; i<16; i++)
			{
				key16Comb[i] = ( (key16[i][0] << 28) | key16[i][1] ); 
			}
		}
		
		
		//final permutation of 16 56-bit keys into 48-bit keys
		//key16Final contains the 16 keys, each 48-bit long, and stored in the lower part of the long
		public void keyPermuteFinal(){
			
			for(int j=0; j<16; j++){
			//for one key, all its permutations are generated;
				for(int i= 0; i<48; i++){
					key16Final[j] = 	( (key16Final[j] << 1) | ( ( (key16Comb[j] << (PC2[i] + 8 - 1) ) & 0x8000000000000000L) >>> 63 ) );				
				}
			}
		}
		
		
		
		// //////////////////////////////Now dealing with data////////////////////////////////////////////////
		
		//doing the initial permutation of data M
		public void mInitPerm(){
			for(int i= 0; i<64; i++){
				dataInitPerm = 	( (dataInitPerm << 1) | ( ( (data << (IP[i] - 1) ) & 0x8000000000000000L) >>> 63 ) );
				System.out.println("The i value is :" + i + " and dataInitPerm value is :" + dataInitPerm);
			}
		}
		
		//left half of permuted data stored in leftHalf
				public void leftDataHalf(){
					leftHalf = (dataInitPerm & 0xffffffff00000000L);			
					leftHalf = (leftHalf >>> 32);
				}
				
		//right half of data stored in rightHalf
				public void rightDataHalf(){
					rightHalf = (dataInitPerm & 0x00000000ffffffffL);	
				}
		
		
				//helper file for func. Performs substitution box lookup//
				//newXorData is 48 bits, stored in a long//
				public long mySubstitution(long newXorData){
					long data = newXorData ; //48 bits
					long[] b = new long[8]; //b will contain the eight blocks, each 6-bit long
					long sb = 0;
					
					for(int i=0; i<8; i++){
						b[i] = ((newXorData << (16 + (i*6))) & 0xFC00000000000000L) ;
					}
					
					for(int i=0; i<8; i++){
						int row;
						int column;
						
						row = (int) (b[i] & 0x21L);
						column = (int) (b[i] & 0x1eL);
						
						b[i] = S[i][row][column]; //now b will contain the 4 bits in the specific row and column
						
						//At this point, we have create the 8 substituted elements into array B 
						// which we combine into a single long sb
					}
					
					for(int i=0; i<8; i++)
					{
						sb = ((sb << 4)  | b[i] );
					}
					return sb;
					
				}
				
				
				//helper function for dataComputer()
				//takes the rightHalf and the key and computes function f//
				public long func(long rightHalf1, long key1){
					long rightHalf = rightHalf1;
					long key = key1;
					long newData = 0;  //newData is E(Rn-1) //
					long newXorData = 0; // newXorData = Kn + E(Rn-1) as in DES algo//
					long newSubData = 0; // is output of "S1(B1)S2(B2)S3(B3)S4(B4)S5(B5)S6(B6)S7(B7)S8(B8)"
					long f = 0;	 // the variable f is the same as defined in DES algorithm by Grabbe//

					
					for(int i= 0; i < 48; i++){
						newData = 	( (newData << 1) | ( ( (rightHalf << (E[i] - 1 + 32) ) & 0x8000000000000000L) >>> 63 ) );
						}
					
					newXorData = (key ^ newData) ; //newXorData = K1+E(R0)
					
					newSubData = mySubstitution(newXorData) ; //substituted data ready for final permutation. The newSubData is 32 bits long
					//newSubData is 32-bit long
					//P is 32 bits
					
					// the variable f is the same as defined in DES algorithm by Grabbe//
					for(int i =0; i<32; i++){
						f = 	( (f << 1) | ( ( (newSubData << (P[i] + 32 - 1) ) & 0x8000000000000000L) ) );				
					}
					
					return f;
				}
				
				//16 iterations of data using f function and key//
				//dataArray[][0 or 1] - 0 means left, and 1 means right.//
				public long dataCompute(){
					
					dataArray[0][0] = rightHalf;
					dataArray[0][1] = (leftHalf ^ (func(rightHalf, key16Final[0]) ) );
					
					
					for(int i= 1; i<16; i++){
						dataArray[i][0] = dataArray[i-1][1];
						dataArray[i][1] = ( (dataArray[i-1][0]) ^ (func(dataArray[i-1][1], key16Final[i])) );
					}
					// We now have L_16 and R_16 stored in dataArray[15][0] and dataArray[15][1] respectively, 
					// each 32 bits long.
					
					//R16L16 is as defined in DES algorithm
					R16L16 = (R16L16 | (dataArray[15][1])); //right half pushed at the lower 32-bit end
					R16L16 = R16L16 << 32; //right half R16 moved to upper 32 bit
					R16L16 = ( (R16L16) | (dataArray[15][0]) ) ;
					//We now have R16L16 as defined in DES algorithm
					
					//Final Permutation//
					
					for(int i= 0; i<64; i++){
						 dataEncrypted = 	( (dataEncrypted << 1) | ( ( (R16L16 << (IP1[i] - 1) ) & 0x8000000000000000L) >>> 63 ) );				
					}
					//dataEncrypted is the final permuted data
					
					return dataEncrypted;
				}
				
				public long encryption()
				{
					keyPlus();
					keyPlusRight();
					keyPlusLeft();
					keyGenerate();
					keyCombine();
					keyPermuteFinal();
					mInitPerm();
					leftDataHalf();
					rightDataHalf();
					dataCompute();
					return dataEncrypted;
				}
		
		
		
		
	
		
	

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		//System.out.println("Test");
		System.out.println("Hello World");
		
		/*
		FileInputStream in = null;
		FileOutputStream out = null;
		Long key = Long.valueOf(args[0]);
		in = new FileInputStream(args[1]);
		out = new FileOutputStream(args[2]);
		long data = 0;
		long encryptData = 0;
		
		while (!(in.available() == 0)){
			byte[] block = new byte[8];
			in.read(block, 0, 8);
					
			for(int i=0; i<8; i++){
				data = (data << 8) | block[i];
			}
			DES des = new DES(data, key);
			encryptData = des.encryption();
			String output = Long.toHexString(encryptData);
		}
		*/
		
		// Test Code//
		
		long M = 0x123456789ABCDEFl;
		long K = 0x133457799BBCDFF1l;
		
		/*
		System.out.println("M is :" + m + " and K is :" + K);
		
		long bitmask = 0x1F;
		System.out.println("Bitmask is " + bitmask);
		*/
		
		DES des = new DES(M, K);
		Long encryptData = des.encryption();
		String output = Long.toHexString(encryptData);
		System.out.println("The encrypted data is :" + output);
		// Test Code
		
		}
	}


