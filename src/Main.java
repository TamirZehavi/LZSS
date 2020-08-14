import java.io.IOException;

public class Main {

	public static void main(String[] args) throws ClassNotFoundException {
		
//		GUI gui = new GUI();
		
		String inPath = "D:\\USER\\Downloads\\Files To Compress\\Romeo and Juliet  Entire Play.txt";
		String outPath = "D:\\USER\\Downloads\\Files To Compress\\COMPRESSED2.txt";
		String decompressedFilePath = "D:\\USER\\Downloads\\Files To Compress\\DECOMPRESSED2.txt";
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
