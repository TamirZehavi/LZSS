import java.beans.Encoder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.BitSet;


public class LZSS {
	public byte[] readFileToBytes(String sourcePath) throws IOException
	{
		File inputFile = new File(sourcePath);
		return Files.readAllBytes(inputFile.toPath());
	}
	
	public void compress(byte[] source, int windowSize, int maxMatch, int minMatch) 
	{
		//you're gonna have to write the window size, and the max match size at the start of the file, so you know how to decode it later
		StringBuilder encodedData = WriteInitialDictionary(source, minMatch);
		StringBuilder searchBuffer = WriteInitialDictionary(source, minMatch);
		int sourcePosition = minMatch;
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
				WriteCharacter(match, encodedData, source, sourcePosition);
			}
			sourcePosition = sourcePosition + match.length; 
			AdjustWindow(match, searchBuffer, windowSize);
		}
		
		
	}
	
	public BitSet ConvertToBits(ByteArrayOutputStream encodedData, StringBuilder dataFlags, int windowSize)
	{
		BitSet encodedBits = new BitSet(encodedData.size() + dataFlags.length());
		byte[] encodedDataByteArray = encodedData.toByteArray();
		int numberOfBytesToWrite = (int) Math.ceil( LogBaseTwo(windowSize)/8 );
		int indexOfEncodedBytes = 0;
		int indexOfBitset = 0;
		
		for(int i=0 ; i < dataFlags.length() ; i++)
		{
			if(dataFlags.charAt(i) == '1')
			{
				encodedBits.set(indexOfBitset);
			}
			indexOfBitset ++;
			
			for(int j=0 ; j < 8 ; j++)
			{
				if(encodedDataByteArray[indexOfEncodedBytes] == '1')
				{
					
				}
			}
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
		encodedData.append(ConvertToBinaryByteString(match.offset));
		encodedData.append(ConvertToBinaryByteString(match.length));	
	}
	
	public void WriteCharacter(Match match, StringBuilder encodedData, byte[] source, int sourcePosition)
	{	
		for(int i = 0; i < match.length; i ++)
		{
			encodedData.append('0');
			encodedData.append(ConvertToBinaryByteString((char)source[sourcePosition]));
			sourcePosition++;
		}	
	}
	
	public String ConvertToBinaryByteString(int parameter)
	{
		String binaryRepresentation = Integer.toBinaryString(parameter);
		
		while(binaryRepresentation.length() % 8 != 0)
		{
			binaryRepresentation = '0' + binaryRepresentation;
		}
		return binaryRepresentation;
	}
	
	public String ConvertToBinaryByteString(char parameter)
	{
		String binaryRepresentation = Integer.toBinaryString(parameter);
		
		while(binaryRepresentation.length() % 8 != 0)
		{
			binaryRepresentation = '0' + binaryRepresentation;
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
				if (match.length == 0)
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
	
	
}


