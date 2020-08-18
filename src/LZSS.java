import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.BitSet;


public class LZSS {
	
	protected String inPath = null;
	protected String outPath = null;
	protected int windowSize = 0;
	protected int maxMatch = 0;
	protected int minMatch = 0;
	private File inFile = null;
	private int numberOfBytesPerOffset = 0;
	protected int redundantBits = 0;
	protected int sourceSize = 0;
	protected int bitsSourcePosition = 0;
	protected float compressionRatio = 0;
	
	public LZSS(String inPath, String outPath, int windowSize, int maxMatch, int minMatch)
	{
		this.inPath = inPath;
		this.outPath = outPath;
		this.inFile = new File(inPath);
		this.windowSize = windowSize;
		this.maxMatch = maxMatch;
		this.minMatch = minMatch;
		this.numberOfBytesPerOffset = (int) (Math.ceil(LogBaseTwo(this.windowSize)/8f));
	}

	public void Compress() throws IOException
	{
		byte[] source = Files.readAllBytes(this.inFile.toPath());
		StringBuilder encodedData = WriteInitialEncoding(source, this.minMatch);
		StringBuilder searchBuffer = WriteInitialDictionary(source, this.minMatch);
		int sourcePosition = this.minMatch;
		
		Encode(source, this.windowSize, this.maxMatch, this.minMatch, encodedData, searchBuffer, sourcePosition);
		
		WriteRedundantBits(encodedData);

		BitSet encodedBits = ConvertToBits(encodedData);
		
		CalculateRatio(encodedBits, source);
		
		WriteToFile(encodedBits);
	}	

	public void CalculateRatio(BitSet encodedBits, byte[] source) 
	{
		float encodedSize = (encodedBits.size() - this.redundantBits) / 8;
		float originalSize = source.length;
		this.compressionRatio = originalSize / encodedSize;
	}
	
	public float GetRatio()
	{
		return this.compressionRatio;
	}

	private void WriteRedundantBits(StringBuilder encodedData)
	{
		this.redundantBits = 64 - ((encodedData.length() + 8) % 64);
		encodedData.insert(0, ConvertToBinaryByteString(this.redundantBits, false));
	}

	public void WriteToFile(BitSet encodedBits) throws IOException {
		
		FileOutputStream writer = new FileOutputStream(this.outPath);
		ObjectOutputStream objectWriter = new ObjectOutputStream(writer);
		objectWriter.writeObject(encodedBits);
		objectWriter.close();
		
	}

	public void Encode(byte[] source, int windowSize, int maxMatch, int minMatch, StringBuilder encodedData,
			StringBuilder searchBuffer, int sourcePosition)
	{
		Match match= new Match(0,0, "");
		while (sourcePosition < source.length)
		{
			match = FindMatch(source, minMatch, match, searchBuffer, sourcePosition, maxMatch);
			if(match.length>minMatch)
			{
				WriteMatch(match, encodedData);
			}
			else
			{
				WriteCharacter(match.length, encodedData, source, sourcePosition);
			}
			sourcePosition = sourcePosition + match.length; 
			AdjustWindow(match, searchBuffer, windowSize);
		}
	}

	public BitSet ConvertToBits(StringBuilder encodedData)
	{
		BitSet encodedBits = new BitSet(encodedData.length());
		int nextIndexOfOne = encodedData.indexOf("1", 0);
		
		while( nextIndexOfOne != -1)
		{
			encodedBits.set(nextIndexOfOne);
			nextIndexOfOne++;
			nextIndexOfOne = encodedData.indexOf("1", nextIndexOfOne);
		}
		
		return encodedBits;
	}
	
	public int LogBaseTwo(int parameter)
	{
		return (int)(Math.log(parameter) / Math.log(2));
	}
	
	public void AdjustWindow(Match match, StringBuilder searchBuffer, int windowSize) 
	{
		for(int i = 0 ; i < match.length ; i ++)
		{
			if(searchBuffer.length() >= windowSize)
				searchBuffer.deleteCharAt(0);
			searchBuffer.append(match.value.charAt(i));
		}
	}

	public void WriteMatch(Match match, StringBuilder encodedData) 
	{
		encodedData.append('1');
		encodedData.append(ConvertToBinaryByteString(match.offset, true));
		encodedData.append(ConvertToBinaryByteString(match.length, false));	
	}

	public void WriteCharacter(int matchLength, StringBuilder encodedData, byte[] source, int sourcePosition)
	{	
		for(int i = 0 ; i < matchLength ; i ++)
		{
			encodedData.append('0');
			int byteAsUnsignedInt = Byte.toUnsignedInt((source[sourcePosition])); 
			encodedData.append(ConvertToBinaryByteString(byteAsUnsignedInt,false));
			sourcePosition++;
		}	
	}
	
	public String ConvertToBinaryByteString(int parameter, boolean isItAnOffset)
	{
		String binaryRepresentation = Integer.toBinaryString(parameter);
		
		if(isItAnOffset && this.numberOfBytesPerOffset > 1)
		{
			while(binaryRepresentation.length() % 16 != 0)
			{
				binaryRepresentation = '0' + binaryRepresentation;
			}
		}
		else
		{
			while(binaryRepresentation.length() % 8 != 0)
			{
				binaryRepresentation = '0' + binaryRepresentation;
			}
		}
		
		
		return binaryRepresentation;
	}
	
	public Match FindMatch(byte[] source, int minMatch, Match match, StringBuilder searchBuffer, int sourcePosition, int maxMatch)
	{
		match.Reset();
		byte[] dataChunk = Arrays.copyOfRange(source, sourcePosition, sourcePosition + maxMatch);
		int dataChunkOffset = 0;
		
		while(match.value.length()<maxMatch-1)
		{
			char nextChar = (char)(Byte.toUnsignedInt(dataChunk[dataChunkOffset])); 
			
			if(searchBuffer.toString().contains(match.value + nextChar))
			{
				match.AddByte(dataChunk[dataChunkOffset]);
				match.offset = searchBuffer.indexOf(match.value);
				match.length ++;
				dataChunkOffset ++;
			}
			else
			{
				if(match.length == 0)
				{
					match.SetLength(1);
					match.AddByte(source[sourcePosition]);
				}
				return match;
			}
		}
		return match;
	}
	
	public StringBuilder WriteInitialDictionary(byte[] source, int minMatch)
	{
		StringBuilder searchBuffer = new StringBuilder();
		
		for(int i=0;i<minMatch;i++)
		{
			searchBuffer.append((char)(Byte.toUnsignedInt(source[i])));
		}
		
		return searchBuffer;
	}
	
	private StringBuilder WriteInitialEncoding(byte[] source, int minMatch) {
		
		StringBuilder encodedData = new StringBuilder();
		
		String windowSizeBits = ConvertToBinaryByteString(LogBaseTwo(this.windowSize), false);
		String maxMatchBits = ConvertToBinaryByteString(LogBaseTwo(this.maxMatch), false);
		
		encodedData.append(windowSizeBits);
		encodedData.append(maxMatchBits);
		
		WriteCharacter(minMatch, encodedData, source, 0);
		
		return encodedData;
	}
	
	public void Decompress() throws IOException, ClassNotFoundException
	{
		BitSet source = ReadBitSetFromFile();
		this.bitsSourcePosition = 0;
		GetInitialData(source, bitsSourcePosition);
		StringBuilder decodedData = new StringBuilder();
		StringBuilder searchBuffer = new StringBuilder();
		Decode(source, decodedData, searchBuffer);
		WriteToFile(decodedData);
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

	private void Decode(BitSet source, StringBuilder decodedData, StringBuilder searchBuffer) 
	{
		Match match = new Match(0,0, "");
		
		while(this.bitsSourcePosition < this.sourceSize)
		{
			match.Reset();
			if(source.get(this.bitsSourcePosition))
			{
				this.bitsSourcePosition++;
				match.SetOffset(ReadOffset(source));
				match.SetLength(ReadNextByte(source));
				match.SetValue(searchBuffer.substring(match.offset, match.offset + match.length));
				decodedData.append(match.value);
			}
			else
			{
				this.bitsSourcePosition++;
				match.SetLength(1);
				match.AddByte((byte)ReadNextByte(source));
				decodedData.append(match.value);
			}
			AdjustWindow(match, searchBuffer, this.windowSize);
		}
	}

	private BitSet ReadBitSetFromFile() throws IOException, ClassNotFoundException 
	{
		FileInputStream input = new FileInputStream(this.inPath);
		ObjectInputStream objectInput = new ObjectInputStream(input);
		BitSet restoredDataInBits = (BitSet) objectInput.readObject();
		objectInput.close();
		return restoredDataInBits;
	}

	private void GetInitialData(BitSet source, int sourcePosition) throws IOException 
	{
		this.sourceSize = source.size() - ReadNextByte(source);
		this.windowSize = (int)(Math.pow(2, ReadNextByte(source)));
		this.maxMatch = (int)(Math.pow(2, ReadNextByte(source)));
		this.numberOfBytesPerOffset = (int) (Math.ceil(LogBaseTwo(this.windowSize)/8f));
	}
	
	public int ReadOffset(BitSet source)
	{
		int nextOffset = 0;
		
		if(this.numberOfBytesPerOffset > 1) //read two bytes as offset
		{
			for(int i = 15 ; i >= 0 ; i--)
			{
				if(source.get(this.bitsSourcePosition))
				{
					nextOffset += Math.pow(2, i);
					this.bitsSourcePosition++;
				}
				else
					this.bitsSourcePosition++;
			}
		}
		else 
		{
			nextOffset = ReadNextByte(source); //read one byte as offset
		}
		
		return nextOffset;
	}
	
	public int ReadNextByte(BitSet source)
	{
		int nextByte = 0;
		for(int i = 7 ; i >= 0 ; i--)
		{
			if(source.get(this.bitsSourcePosition))
			{
				nextByte += Math.pow(2, i);
				this.bitsSourcePosition++;
			}
			else
				this.bitsSourcePosition++;
		}
	
		return nextByte;
	}
	
}


