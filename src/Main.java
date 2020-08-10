import java.io.ByteArrayOutputStream;

public class Main {

	public static void main(String[] args) {
		LZSS lzss = new LZSS();
		byte[] source = {'a','b','r','a','a','b','r','a','a','b','r','a'};
		lzss.compress(source, 4096, 8, 2);
		StringBuilder wow = new StringBuilder();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		System.out.println(ConvertToBinaryByteString(256));
	}
	
	public static String ConvertToBinaryByteString(int parameter)
	{
		String binaryRepresentation = Integer.toBinaryString(parameter);
		
		while(binaryRepresentation.length() % 8 != 0)
		{
			binaryRepresentation = '0' + binaryRepresentation;
		}
		return binaryRepresentation;
	}

}
