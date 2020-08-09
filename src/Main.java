import java.io.ByteArrayOutputStream;

public class Main {

	public static void main(String[] args) {
		StringBuilder wow = new StringBuilder();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int match = 1;
		System.out.println(Integer.toBinaryString(match));
		wow.append(String.valueOf(match).getBytes());
		out.write(match);
		System.out.println();
	}

}
