import java.io.IOException;

public class Main {

	public static void main(String[] args) throws ClassNotFoundException {
		
//		GUI gui = new GUI();
		
		String inPath = "D:\\USER\\Downloads\\Files To Compress\\(2014)null & Scooblee - kotfd [VRC6].mp3";
		String outPath = "D:\\USER\\Downloads\\Files To Compress\\COMPRESSED.txt";
		String decompressedFilePath = "D:\\USER\\Downloads\\Files To Compress\\DECOMPRESSED.mp3";
		int windowSize = 4096;
		int maxMatch = 256;
		int minMatch = 2;
		
		LZSS lzssCompress = new LZSS(inPath, outPath, windowSize, maxMatch, minMatch);
		LZSS lzssDecompress = new LZSS(outPath, decompressedFilePath, windowSize, maxMatch, minMatch);
		try {
			lzssCompress.Compress();
			lzssDecompress.Decompress();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
