import java.beans.Encoder;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.security.spec.EncodedKeySpec;
import java.util.Arrays;
import java.util.BitSet;


public class LZSS {
	
	protected String inPath = null;
	protected String outPath = null;
	protected int windowSize = 0;
	protected int maxMatch = 0;
	protected int minMatch = 0;
	private File inFile = null;
	private File outFile = null;
	private int numberOfBytesPerOffset = 0;
	protected int redundantBits = 0;
	
	public LZSS(String inPath, String outPath, int windowSize, int maxMatch, int minMatch)
	{
		this.inPath = inPath;
		this.outPath = outPath;
		this.inFile = new File(inPath);
		this.outFile = new File(outPath);
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
		Match match= new Match(0,0, "");
		
		Encode(source, this.windowSize, this.maxMatch, this.minMatch, encodedData, searchBuffer, sourcePosition, match);
		
		WriteRedundantBits(encodedData);
		
		BitSet encodedBits = ConvertToBits(encodedData);
		
		WriteToFile(encodedBits);
		
	}	

	private void WriteRedundantBits(StringBuilder encodedData)
	{
		AddToRedundantBits(8);
		this.redundantBits = 64 - this.redundantBits;
		encodedData.insert(0, ConvertToBinaryByteString(this.redundantBits, false));
	}

	public void WriteToFile(BitSet encodedBits) throws IOException {
		
		FileOutputStream writer = new FileOutputStream(this.outPath);
		ObjectOutputStream objectWriter = new ObjectOutputStream(writer);
		objectWriter.writeObject(encodedBits);
		objectWriter.close();
		
	}

	public void Encode(byte[] source, int windowSize, int maxMatch, int minMatch, StringBuilder encodedData,
			StringBuilder searchBuffer, int sourcePosition, Match match)
	{
		while (sourcePosition < source.length)
		{
			match = FindMatch(source, minMatch, match, searchBuffer, sourcePosition, maxMatch);
			if(match.length>minMatch)
			{
				WriteMatch(match, encodedData);
			}
			else
			{
				WriteCharacter(match, encodedData, source, sourcePosition);
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
		for(int i=0; i<match.length; i++)
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
		AddToRedundantBits(1 + (8 * this.numberOfBytesPerOffset));
		encodedData.append(ConvertToBinaryByteString(match.length, false));	
		AddToRedundantBits(9);
	}

	public void WriteCharacter(Match match, StringBuilder encodedData, byte[] source, int sourcePosition)
	{	

		for(int i = 0; i < match.length; i ++)
		{
			encodedData.append('0');
			encodedData.append(ConvertToBinaryByteString((int)((char)source[sourcePosition]),false));
			sourcePosition++;
			AddToRedundantBits(9);
		}	
	}
	
	private void AddToRedundantBits(int i) 
	{
		this.redundantBits = (this.redundantBits + i) % 64;
	}
	
	public String ConvertToBinaryByteString(int parameter, boolean isItAnOffset)
	{
		String binaryRepresentation = Integer.toBinaryString(parameter);
		
		if(isItAnOffset == true)
		{
			if(this.numberOfBytesPerOffset  < 2)
			{
				while(binaryRepresentation.length() % 8 != 0)
				{
					binaryRepresentation = '0' + binaryRepresentation;
				}
			}
			else
			{
				while(binaryRepresentation.length() % 16 != 0)
				{
					binaryRepresentation = '0' + binaryRepresentation;
				}
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
		String dataChunk = new String(Arrays.copyOfRange(source, sourcePosition, sourcePosition + maxMatch));
		int dataChunkOffset = 0;
		
		while(match.value.length()<maxMatch)
		{
			if(searchBuffer.toString().contains(match.value + dataChunk.charAt(dataChunkOffset)))
			{
				match.value += dataChunk.charAt(dataChunkOffset);
				match.offset = searchBuffer.indexOf(match.value);
				match.length ++;
				dataChunkOffset ++;
			}
			else
			{
				if(match.length == 0)
				{
					match.SetLength(1);
					match.AddValue((char)source[sourcePosition]);
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
			searchBuffer.append((char)source[i]);
		}
		
		return searchBuffer;
	}
	
	private StringBuilder WriteInitialEncoding(byte[] source, int minMatch) {
		
		StringBuilder encodedData = new StringBuilder();
		
		String windowSizeBits = ConvertToBinaryByteString(LogBaseTwo(this.windowSize), false);
		String maxMatchBits = ConvertToBinaryByteString(LogBaseTwo(this.maxMatch), false);
		
		encodedData.append(windowSizeBits);
		encodedData.append(maxMatchBits);
		AddToRedundantBits(windowSizeBits.length() + maxMatchBits.length());
		
		
		
		for(int i = 0; i < minMatch; i ++)
		{
			encodedData.append('0');
			encodedData.append(ConvertToBinaryByteString((int)((char)source[i]), false));
			AddToRedundantBits(9);
		}	
		
		return encodedData;
	}
	
	public void Decompress() throws IOException, ClassNotFoundException
	{
		BitSet source = ReadBitSetFromFile();
		int sourcePosition = 0;
		GetWindowSizes(source);
	}

	private BitSet ReadBitSetFromFile() throws IOException, ClassNotFoundException 
	{
		FileInputStream input = new FileInputStream(this.inPath);
		ObjectInputStream objectInput = new ObjectInputStream(input);
		BitSet restoredDataInBits = (BitSet) objectInput.readObject();
		objectInput.close();
		return restoredDataInBits;
	}

	private void GetWindowSizes(BitSet source) throws IOException 
	{
		String firstbyte = "";
		for(int i=0;i<8;i++)
		{
			if(source.get(i))
			{
				firstbyte+="1";
			}
			else
			{
				firstbyte+="0";
			}
		}
	}
	
	public int ReadNextByte(int sourcePosition, BitSet source)
	{
		int nextByte = 0;
		for(int i = 8 ; i > 0 ; i--)
		{
			if(source.get(sourcePosition))
			{
				nextByte += Math.pow(2, i);
			}
		}
		return 0;
	}
	
}


