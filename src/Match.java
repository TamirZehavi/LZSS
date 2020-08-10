
public class Match {
	protected int length;
	protected int offset;
	protected String value;
	
	public Match(int length, int offset, String value)
	{
		this.length=length;
		this.offset=offset;
		this.value = value;
	}
	
	public void SetOffset(int offset) { this.offset = offset; }
	public void SetLength(int length) { this.length = length; }
	public void SetValue(String value) { this.value = value; }
	public void AddValue(char value) { this.value += value; }
	
	public void Reset()
	{
		this.offset = 0;
		this.length = 0;
		this.value = "";
	}
}
