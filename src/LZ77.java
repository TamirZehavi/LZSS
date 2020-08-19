import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.util.BitSet;

public class LZ77 {
	private String inPath = null;
	private String outPath = null;
	private File inFile;
	private File outFile;
	private final int windowSize;
	private final int lookaheadBufferSize;	
	private final int searchBufferSize;
	private int nextByteIndex = 0;
	private int nextBitIndex = 0;
	private int currentSearchBufferSize = 0;
	private int currentLookaheadBufferSize = 0;
	private int appendToWindowBuffer = 0;
	private byte[] source = null;
	private float compressionRatio;

	public LZ77(String inPath,String outPath,int windowSize,int lookaheadBufferSize) throws IOException
	{
		this.inPath = inPath;
		this.outPath = outPath;
		this.inFile = new File(inPath);
		this.outFile = new File(outPath);
		this.windowSize = windowSize;
		this.lookaheadBufferSize = lookaheadBufferSize;
		this.searchBufferSize = windowSize - lookaheadBufferSize;
		this.source = Files.readAllBytes(inFile.toPath());
	}
	public void compress() throws IOException
	{
		StringBuilder dictionary = new StringBuilder();
		bufferInitialize(dictionary);
		StringBuilder compressed = new StringBuilder();		
		encode(dictionary,compressed);
		addSizeBitsMod64(compressed);
		writeFile(compressed);		
	}
	private void bufferInitialize(StringBuilder dictionary)
	{		
		for (int i = 0; i < lookaheadBufferSize; i++) {
			if(source.length>nextByteIndex) {
				dictionary.append((char)Byte.toUnsignedInt(source[nextByteIndex]));
				nextByteIndex++;
				currentLookaheadBufferSize++;				
			}
			else
			{
				break;
			}
		}
	}
	public void encode(StringBuilder dictionary,StringBuilder compressed)
	{
		while(currentLookaheadBufferSize > 0)
		{
			Match match = findMatch(dictionary);
			WriteMatch(compressed,match.offset,match.length,dictionary.charAt(currentSearchBufferSize + match.length));
			appendToWindowBuffer = increaseBuffer(match.length);
			appendBuffer(dictionary);
		}
	}
	private Match findMatch(StringBuilder dictionary)
	{
		Match match= new Match(0,0, "");
		String matchedString = null;
		int offset;
		int matchLookAheadIndex = currentSearchBufferSize;
		if(haveAnyMatch(dictionary))
		{
			matchedString = "" + dictionary.charAt(matchLookAheadIndex);
			offset = findMatchIndex(dictionary,matchedString);
			while(offset != -1 && matchLookAheadIndex < dictionary.length() - 1)
			{
				match.SetLength(match.length + 1);
				match.SetOffset(offset);
				match.SetValue(matchedString);
				matchLookAheadIndex++;
				matchedString +=dictionary.charAt(matchLookAheadIndex);
				offset = findMatchIndex(dictionary,matchedString);
			}
		}
		return match;
	}
	private int findMatchIndex(StringBuilder dictionary,String value)
	{
		int stringLength = value.length();
		String tmpMatch = null;
		int offsetMatch;
		for (int i = currentSearchBufferSize - 1; i >=0; i--) 
		{
			tmpMatch = dictionary.substring(i, i +stringLength );
			offsetMatch = currentSearchBufferSize - i;
			if(tmpMatch.equals(value))
			{
				return offsetMatch;
			}
		}
		return -1;
	}
	private boolean haveAnyMatch(StringBuilder dictionary)
	{
		if (currentSearchBufferSize == 0)
		{
			return false;
		}
		if(!isExistInSearchBuffer(dictionary,dictionary.charAt(currentSearchBufferSize)))	
		{
			return false;
		}
		return true;
	}
	private boolean isExistInSearchBuffer(StringBuilder dictionary, char isCharAtDictionary)
	{
		for (int i = 0; i < currentSearchBufferSize; i++) {
			if(dictionary.charAt(i) == isCharAtDictionary)
			{
				return true;
			}
		}
		return false;
	}
	private int increaseBuffer(int matchLength)
	{
		return 1 + matchLength;
	}
	private int findBitSize(int decimalNumber) {
		if(decimalNumber >= 256)
		{
			return 16;
		}
		else
		{
			return 8;
		}
	}
	public void convertStringToBitSet(StringBuilder compressed,BitSet encodedBits)
	{
		for (int i = 0; i < compressed.length(); i++) {
			if(compressed.charAt(i)==1)
			{
				encodedBits.set(i);
			}
		}
	}
	private BitSet ConvertToBits(StringBuilder compressed)
	{
		BitSet encodedBits = new BitSet(compressed.length());
		int nextIndexOfOne = compressed.indexOf("1", 0);
		while( nextIndexOfOne != -1)
		{
			encodedBits.set(nextIndexOfOne);
			nextIndexOfOne++;
			nextIndexOfOne = compressed.indexOf("1", nextIndexOfOne);
		}		
		return encodedBits;
	}
	private void writeFile(StringBuilder compressed) throws IOException
	{
		BitSet encodedBits = new BitSet(compressed.length());
		encodedBits = ConvertToBits(compressed);
		FileOutputStream writer = new FileOutputStream(this.outPath);
		ObjectOutputStream objectWriter = new ObjectOutputStream(writer);
		objectWriter.writeObject(encodedBits);
		CalculateRatio(encodedBits,source);
		objectWriter.close();
	}
	private void appendBuffer(StringBuilder dictionary)
	{
		for (int i = 0; i < appendToWindowBuffer && i < source.length; i++) {
			if(ableDeleteChar(dictionary))
			{
				dictionary.deleteCharAt(0);
			}
			if(nextByteIndex<source.length)
			{
				char nextByte = (char)Byte.toUnsignedInt(source[nextByteIndex]);
				dictionary.append(nextByte);
				nextByteIndex++;
			}
			else
			{
				currentLookaheadBufferSize--;
			}
			if(currentSearchBufferSize < searchBufferSize)
			{
				currentSearchBufferSize++;
			}
		}
		appendToWindowBuffer = 0;
	}
	private void WriteMatch(StringBuilder compressed,int offset, int length, char character)
	{
		String offsetInBits = writeInt(offset);
		String LengthInBits = writeInt(length);
		String characterInBits = writeChar(character);
		String totalBits = offsetInBits + LengthInBits + characterInBits;
		compressed.append(totalBits);
	}
	private String writeInt(int decimalNumber)
	{
		int BitSizeCheck = findBitSize(decimalNumber);
		StringBuilder binaryString = new StringBuilder();
		binaryString.append(convertNumToBinaryString(decimalNumber));
		while (binaryString.length() < BitSizeCheck)
		{
			binaryString.insert(0, "0");
		}
		if(BitSizeCheck == 8)
		{
			binaryString.insert(0, "0");
		}
		else
		{
			binaryString.insert(0, "1");
		}		
		return binaryString.toString();
	}
	private String convertNumToBinaryString(int decimalNumber)
	{	
		return Integer.toString(decimalNumber, 2);
	}
	private String writeChar(char character)
	{
		StringBuilder binaryString = new StringBuilder();
		binaryString.append(convertNumToBinaryString((int)character));
		while (binaryString.length() < 8)
		{
			binaryString.insert(0, "0");
		}
		return binaryString.toString();
	}
	private boolean ableDeleteChar(StringBuilder dictionary)
	{
		if(dictionary.length() == windowSize )
		{
			return true;
		}
		if(currentLookaheadBufferSize < lookaheadBufferSize)
		{
			if(currentSearchBufferSize == searchBufferSize)
			{
				return true;
			}
		}
		return false;
	}
	private void addSizeBitsMod64(StringBuilder compressed)
	{
		int bitsLeft = compressed.length()%64;
		String bitsLeftBinary = writeInt(bitsLeft);
		compressed.insert(0, bitsLeftBinary);
	}
	public void decompress () throws ClassNotFoundException, IOException
	{
		BitSet source = readObjectFile();
		StringBuilder decompress = new StringBuilder ();		
		int bitSetLength = findLengthBitSet(source);
		decode(decompress,bitSetLength,source);	
		WriteToFile(decompress);
	}
	private BitSet readObjectFile() throws IOException, ClassNotFoundException 
	{
		FileInputStream input = new FileInputStream(this.inPath);
		ObjectInputStream objectInput = new ObjectInputStream(input);
		BitSet restoredDataInBits = (BitSet) objectInput.readObject();
		objectInput.close();
		return restoredDataInBits;
	}
	private void decode(StringBuilder decompress, int bitSetLength,BitSet source)
	{
		while(nextBitIndex < bitSetLength)
		{
			Match match = convertBitsToMatch(source);
			addDecode(decompress, match);
		}
	}
	private void addDecode(StringBuilder decompress, Match match)
	{
		int RelativeOffset;
		char decodeChar;

		if(match.length == 0 && match.offset == 0)
		{
			decompress.append(match.value);	
		}
		else
		{	
			RelativeOffset = decompress.length() - match.offset;
			for (int i = 0; i < match.length; i++) {
				decodeChar = decompress.charAt(RelativeOffset);
				decompress.append(decodeChar);
				RelativeOffset++;
			}
			decompress.append(match.value);
		}
	}
	private Match convertBitsToMatch(BitSet source)
	{
		int offset;
		int length;
		char character;
		if(source.get(nextBitIndex) == false)
		{
			nextBitIndex++;
			offset = findOffsetLengthMatch(8,source);
		}
		else
		{
			nextBitIndex++;
			offset = findOffsetLengthMatch(16,source);
		}
		if(source.get(nextBitIndex) == false)
		{
			nextBitIndex++;
			length = findOffsetLengthMatch(8,source);
		}
		else
		{
			nextBitIndex++;
			length = findOffsetLengthMatch(16,source);
		}

		character = findCharacterMatch(source);
		Match match = new Match(length,offset,""+character);
		return match;
	}
	private int findOffsetLengthMatch(int index, BitSet source)
	{
		StringBuilder offsetLengthBinary = new StringBuilder();
		for (int i = 0; i < index; i++) {
			if(source.get(nextBitIndex) == false)
			{
				offsetLengthBinary.append('0');
				nextBitIndex++;
			}
			else
			{
				offsetLengthBinary.append('1');
				nextBitIndex++;
			}
		}
		int offsetLengthDecimal = convertBinaryStringToDecimal(offsetLengthBinary);
		return offsetLengthDecimal;
	}
	private char findCharacterMatch(BitSet source)
	{
		StringBuilder charBinary = new StringBuilder();
		for (int i = 0; i < 8; i++) {
			if(source.get(nextBitIndex) == false)
			{
				charBinary.append('0');
				nextBitIndex++;
			}
			else
			{
				charBinary.append('1');
				nextBitIndex++;
			}
		}
		char charDecimal = (char)convertBinaryStringToDecimal(charBinary);
		return charDecimal;
	}
	private int findLengthBitSet(BitSet source)
	{
		StringBuilder lengthBinary = new StringBuilder();
		for (int i = 0; i < 9; i++) {
			if(source.get(i) == false)
			{
				lengthBinary.append('0');
				nextBitIndex++;
			}
			else
			{
				lengthBinary.append('1');
				nextBitIndex++;
			}
		}
		int lengthModule = convertBinaryStringToDecimal(lengthBinary);
		int lengthNotUsed = 64 - lengthModule;
		int fullLength = source.size() - lengthNotUsed + 9 ;
		return fullLength;
	}
	private int convertBinaryStringToDecimal(StringBuilder lengthBinary)
	{
		int length = Integer.parseInt(lengthBinary.toString(), 2);
		return length;
	}
	public void writeDecode (StringBuilder decompress) throws IOException
	{
		Writer write = new FileWriter(this.outFile);
		write.write(decompress.toString());
		write.close();
	}
	private void WriteToFile(StringBuilder decodedData) throws IOException
	{
		FileOutputStream outputFileStream = new FileOutputStream(this.outPath); 
		for(int i = 0; i < decodedData.length(); i++)
		{
			byte currentByte = (byte)decodedData.charAt(i);
			outputFileStream.write(currentByte);
		}
		outputFileStream.close();
	}
	private void CalculateRatio(BitSet encodedBits, byte[] source) 
	{
		float encodedSize = (float)findLengthBitSet(encodedBits)/8f;
		float originalSize = source.length;
		this.compressionRatio = originalSize / encodedSize;	
	}
	public float GetRatio()
	{
		return this.compressionRatio;
	}
}
