import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.BitSet;

public class Main {

	public static void main(String[] args) throws ClassNotFoundException {
		
		String inPath = "D:\\USER\\Downloads\\Files To Compress\\test.txt";
		String outPath = "D:\\USER\\Downloads\\Files To Compress\\testCOMP.txt";
		String compressedFilePath = "D:\\USER\\Downloads\\Files To Compress\\testDECOMP.txt";;
		int windowSize = 4096;
		int maxMatch = 256;
		int minMatch = 2;
		
		LZSS lzssCompress = new LZSS(inPath, outPath, windowSize, maxMatch, minMatch);
		LZSS lzssDecompress = new LZSS(outPath, compressedFilePath, windowSize, maxMatch, minMatch);
		try {
			lzssCompress.Compress();
			lzssDecompress.Decompress();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
