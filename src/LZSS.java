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
		ByteArrayOutputStream encodedData = new ByteArrayOutputStream();
		StringBuilder dataFlags = new StringBuilder();
				StringBuilder searchBuffer = WriteInitialDictionary(source, encodedData, minMatch);
		String searchBufferr = new String();
		int sourcePosition = minMatch;
		Match match= new Match(0,0, "");
		
		while (sourcePosition < source.length)
		{
			match = FindMatch(source, minMatch, match, searchBuffer, sourcePosition, maxMatch);
			if(match.length>minMatch)
			{
				WriteMatchToByteArray(dataFlags, match, encodedData);
			}
			else
			{
				WriteCharacterToByteArray(dataFlags, match, encodedData, source, sourcePosition);
			}
			sourcePosition = sourcePosition + match.length; 
			AdjustWindow(match, searchBuffer, windowSize);
		}
		
		
	}
	
	public BitSet ConvertToBits(ByteArrayOutputStream encodedData, StringBuilder dataFlags, int windowSize)
	{
		BitSet encodedBits = new BitSet(encodedData.size() + dataFlags.length());
		byte[] encodedDataByteArray = encodedData.toByteArray();
		int numberOfBytesToWrite = (int) Math.ceil( LogBaseTwo(windowSize)/8 ) ;
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
		for(int i=0; i<match.value.length(); i++)
		{
			if(searchBuffer.length() >= windowSize)
				searchBuffer.deleteCharAt(0);
			searchBuffer.append(match.value.charAt(i));
		}
	}

	public void WriteMatchToByteArray(StringBuilder dataFlags, Match match, ByteArrayOutputStream encodedData) 
	{
		dataFlags.append('1');
		try { 
			encodedData.write(String.valueOf(match.offset).getBytes());
			encodedData.write(String.valueOf(match.length).getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void WriteCharacterToByteArray(StringBuilder dataFlags, Match match, ByteArrayOutputStream encodedData, byte[] source, int sourcePosition)
	{
		dataFlags.append('0');
		for(int i = 0; i < match.length; i ++)
		{
			encodedData.write(source[sourcePosition]);
			sourcePosition++;
		}
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
				dataChunkOffset ++;
				match.value += dataChunk.charAt(dataChunkOffset);
				match.offset = searchBuffer.indexOf(match.value);
				match.length ++;
			}
			else
			{
				return match;
			}
		}
		return match;
	}
	
	public StringBuilder WriteInitialDictionary(byte[] source, ByteArrayOutputStream encodedData, int minMatch)
	{
		for(int i=0;i<minMatch;i++)
		{
			encodedData.write(source[i]);
		}
		
		return new StringBuilder(encodedData.toString());
	}
	
	
}


