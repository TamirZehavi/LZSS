import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.BitSet;

public class Main {

	public static void main(String[] args) throws ClassNotFoundException {
		
//		System.out.println(160%64);
		BitSet bits = new BitSet(8);
		
//		System.out.println(bits.size());
		
		
		String inPath = "D:\\USER\\Downloads\\Files To Compress\\8.8.19.png";
		String outPath = "D:\\USER\\Downloads\\Files To Compress\\testCOMP.txt";
		String decompressedFilePath = "D:\\USER\\Downloads\\Files To Compress\\testDECOMP.png";;
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
