import java.io.Serializable;

public class Player implements Serializable {

	private static final long serialVersionUID = -2878429153614263902L;
	private int pNum;
	private int x;
	private int y;
	
	public Player (int playerNumber, int xCord, int yCord) {
		
		pNum = playerNumber;
		
		x = xCord;
		y = yCord;
		
	}
	
	public int getPNum() {
		
		return pNum;
		
	}
	
	public int getX() {
		
		return x;
		
	}
	
	public int getY() {
		
		return y;
		
	}
	
	public void shiftX(int shift) {
		
		x += shift;
		
	}
	
	public void shiftY(int shift) {
		
		y += shift;
		
	}
	
	public Player clone() {
		
		return new Player(pNum, x, y);
		
	}
	
	public int[] getData() {
		
		return new int[] {pNum, x, y};
		
	}
	
	public void setData(String data) {
		
		data = data.replace("[", "").replace("]", "").replace(" ", "");
		
		String[] allData = data.split(", ");
		
		pNum = Integer.parseInt(allData[0]);
		x = Integer.parseInt(allData[1]);
		y = Integer.parseInt(allData[2]);
		
	}
	
	@Override
	public String toString() {
		
		return ("Player Number: " + pNum + ", X: " + x + ", Y: " + y);
		
	}

}
