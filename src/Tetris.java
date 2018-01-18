import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;


public class Tetris extends JPanel {

    private static long serialVersionUID = -807909536278284335L;
    private static final int BlockSize = 10;
    private static final int BlockWidth = 16;
    private static final int BlockHeight = 26;
    private static final int TimeDelay = 1000; // ms

    private static final String[] AuthorInfo = {
            "Author", "Hall Wood", "Just for fun, lol."
    };

    // to save the exited blocks
    private boolean[][] BlockMap = new boolean[BlockHeight][BlockWidth];

    // score
    private int Score = 0;

    // is pause btn pressed?
    private boolean IsPause = false;

    // shape from Block
    static boolean[][][] Shapes = Block.Shapes;

    // the pos of the falling block
    private Point CurrBlockPos;

    // current existing block map
    private boolean[][] CurrBlockMap;

    // next block map
    private boolean[][] NextBlockMap;

    /**
     *  We have 7 shapes of the blocks;
     *  every block can rotate 4 times to recover the state;
     *  so 4*7 = 28; num belongs to [0, 28)
     *  num % 4 == (the state of the shape)
     *  num / 4 == (the specific shape)
     */
    private int NextBlockState;
    private int CurrBlockState;

    // counter
    private Timer timer;

    public Tetris() {
    	this.init();
    	this.timer = new Timer(Tetris.TimeDelay, this.TimerListener);
    	this.timer.start();
    	this.addKeyListener(this.KeyListener);
    }

    public void setMode(String mode) {
        if(mode.equals("v4")) {
            Tetris.Shapes = Block.Shapes;
        } else {
            // do nothing
        }
        this.init();
        this.repaint();
    }

    // when the new block is falling
    private void getNextBlock() {
        // iterator
        this.CurrBlockMap = this.NextBlockMap;
        this.CurrBlockState = this.NextBlockState;
        // get next state
        this.NextBlockState = this.createNewBlockState();
        this.NextBlockMap = this.getBlockMap(this.NextBlockState);
        // caculate the pos of the block
        this.CurrBlockPos = this.calNewBlockInitPos();
    }

    // if the falling block touch the falled blocks;
    private boolean IsTouch(boolean[][] SrcNextBlockMap, Point SrcNextBlockPos) {
        for (int i = 0; i < SrcNextBlockMap.length; ++i) {
            for (int j = 0; j < SrcNextBlockMap[i].length; ++j) {
                if(SrcNextBlockMap[i][j]) {
                    if(SrcNextBlockPos.y + i >= Tetris.BlockHeight || SrcNextBlockPos.x + j < 0 || SrcNextBlockPos.x + j >= Tetris.BlockWidth)
                        return true; // end if
                    else {
                        if(SrcNextBlockPos.y + i < 0) continue;
                        else if (this.BlockMap[SrcNextBlockPos.y + i][SrcNextBlockPos.x + j])
                            return true;
                    } // end else
                } // end if
            } // end for
        } // end for
        return false;
    }

    // solidify the block on the map
    private boolean  fixBlock(){
        for(int i = 0; i < this.CurrBlockMap.length; ++i) {
            for(int j = 0; j < this.CurrBlockMap[i].length; ++j) {
                if(this.CurrBlockMap[i][j])
                    if (this.CurrBlockPos.y + i < 0)
						return false;
					else this.BlockMap[this.CurrBlockPos.y + i][this.CurrBlockPos.x + j] = this.CurrBlockMap[i][j];
            }
        }
        return true;
    }

    // caculate the pos of the block
    private Point calNewBlockInitPos(){
        return new Point(Tetris.BlockWidth / 2 - this.CurrBlockMap[0].length / 2, - this.CurrBlockMap.length);
    }

    // init
    public void init() {
        // clear map
		for (int i = 0;i < this.BlockMap.length;i ++){
			for (int j = 0;j < this.BlockMap[i].length;j ++){
				this.BlockMap[i][j] = false;
			}
		}
		// clear score
		this.Score = 0;
		// init the first initialized blocks
		this.CurrBlockState = this.createNewBlockState();
		this.CurrBlockMap = this.getBlockMap(this.CurrBlockState);
		this.NextBlockState = this.createNewBlockState();
		this.NextBlockMap = this.getBlockMap(this.NextBlockState);
		// 计算方块位置
		this.CurrBlockPos = this.calNewBlockInitPos();
        this.repaint();
    }

    public void setPause(boolean value){
		this.IsPause = value;
		if (this.IsPause) {
			this.timer.stop();
		} else {
			this.timer.restart();
		}
		this.repaint();
    }

    // emit the state in random
	private int createNewBlockState() {
		int Sum = Tetris.Shapes.length * 4;
		return (int) (Math.random() * 1000) % Sum;
	}

	private boolean[][] getBlockMap(int BlockState) {
		int Shape = BlockState / 4;
		int Arc = BlockState % 4;
		System.out.println(BlockState + "," + Shape + "," + Arc);
		return this.rotateBlock(Tetris.Shapes[Shape], Math.PI / 2 * Arc);
    }

    /**
	 * 旋转方块Map，使用极坐标变换,注意源矩阵不会被改变
	 * 使用round解决double转换到int精度丢失导致结果不正确的问题
	 *
	 * @param BlockMap
	 *            需要旋转的矩阵
	 * @param angel
	 *            rad角度，应该为pi/2的倍数
	 * @return 转换完成后的矩阵引用
	 */
	private boolean[][] rotateBlock(boolean[][] BlockMap, double angel) {
		// get martrix size
		int Heigth = BlockMap.length;
		int Width = BlockMap[0].length;
		// new martrix saving the res
		boolean[][] ResultBlockMap = new boolean[Heigth][Width];
		// calculate the center
		float CenterX = (Width - 1) / 2f;
		float CenterY = (Heigth - 1) / 2f;
		// calculate the state
		for (int i = 0; i < BlockMap.length; i++) {
			for (int j = 0; j < BlockMap[i].length; j++) {
				// calculate the pos that interact to the center
				float RelativeX = j - CenterX;
				float RelativeY = i - CenterY;
				float ResultX = (float) (Math.cos(angel) * RelativeX - Math.sin(angel) * RelativeY);
				float ResultY = (float) (Math.cos(angel) * RelativeY + Math.sin(angel) * RelativeX);
				/* test mesg
				    System.out.println("RelativeX:" + RelativeX + "RelativeY:" + RelativeY);
				    System.out.println("ResultX:" + ResultX + "ResultY:" + ResultY);
				*/
				// fallback the pos
				Point OrginPoint = new Point(Math.round(CenterX + ResultX), Math.round(CenterY + ResultY));
				ResultBlockMap[OrginPoint.y][OrginPoint.x] = BlockMap[i][j];
			}
		}
		return ResultBlockMap;
	}

	/**
	 * test func to test rotate func
	 *
	 * @param args
	 */
	static public void main(String... args) {
		boolean[][] SrcMap = Tetris.Shapes[3];
		Tetris.showMap(SrcMap);
		/*
		for (int i = 0;i < 7;i ++){
			System.out.println(i);
			Tetris.ShowMap(Tetris.Shape[i]);
		}
		*/

		Tetris tetris = new Tetris();
		boolean[][] result = tetris.rotateBlock(SrcMap, Math.PI / 2 * 3);
		Tetris.showMap(result);
    }

    /**
	 * test func to show martrix
	 * @param SrcMap
	 */
	static private void showMap(boolean[][] SrcMap){
		System.out.println("-----");
		for (int i = 0;i < SrcMap.length;i ++){
			for (int j = 0;j < SrcMap[i].length;j ++){
				if (SrcMap[i][j])
					System.out.print("*");
				else
					System.out.print(" ");
			}
			System.out.println();
		}
		System.out.println("-----");
	}

	/**
	 * 绘制游戏界面
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		// 画墙
		for (int i = 0; i < Tetris.BlockHeight + 1; i++) {
			g.drawRect(0 * Tetris.BlockSize, i * Tetris.BlockSize, Tetris.BlockSize, Tetris.BlockSize);
			g.drawRect((Tetris.BlockWidth + 1) * Tetris.BlockSize, i * Tetris.BlockSize, Tetris.BlockSize,
					Tetris.BlockSize);
		}
		for (int i = 0; i < Tetris.BlockWidth; i++) {
			g.drawRect((1 + i) * Tetris.BlockSize, Tetris.BlockHeight * Tetris.BlockSize, Tetris.BlockSize,
					Tetris.BlockSize);
		}
		// 画当前方块
		for (int i = 0; i < this.CurrBlockMap.length; i++) {
			for (int j = 0; j < this.CurrBlockMap[i].length; j++) {
				if (this.CurrBlockMap[i][j])
					g.fillRect((1 + this.CurrBlockPos.x + j) * Tetris.BlockSize, (this.CurrBlockPos.y + i) * Tetris.BlockSize,
						Tetris.BlockSize, Tetris.BlockSize);
			}
		}
		// 画已经固定的方块
		for (int i = 0; i < Tetris.BlockHeight; i++) {
			for (int j = 0; j < Tetris.BlockWidth; j++) {
				if (this.BlockMap[i][j])
					g.fillRect(Tetris.BlockSize + j * Tetris.BlockSize, i * Tetris.BlockSize, Tetris.BlockSize,
						Tetris.BlockSize);
			}
		}
		//绘制下一个方块
		for (int i = 0;i < this.NextBlockMap.length;i ++){
			for (int j = 0;j < this.NextBlockMap[i].length;j ++){
				if (this.NextBlockMap[i][j])
					g.fillRect(190 + j * Tetris.BlockSize, 30 + i * Tetris.BlockSize, Tetris.BlockSize, Tetris.BlockSize);
			}
		}
		// 绘制其他信息
		g.drawString("游戏分数:" + this.Score, 190, 10);
		for (int i = 0;i < Tetris.AuthorInfo.length;i ++){
			g.drawString(Tetris.AuthorInfo[i], 190, 100 + i * 20);
		}

		//绘制暂停
		if (this.IsPause){
			g.setColor(Color.white);
			g.fillRect(70, 100, 50, 20);
			g.setColor(Color.black);
			g.drawRect(70, 100, 50, 20);
			g.drawString("PAUSE", 75, 113);
		}
	}
	/**
	 *
	 * @return
	 */
	private int clearLines(){
		int lines = 0;
		for (int i = 0;i < this.BlockMap.length;i ++){
			boolean IsLine = true;
			for (int j = 0;j < this.BlockMap[i].length;j ++){
				if (!this.BlockMap[i][j]){
					IsLine = false;
					break;
				}
			}
			if (IsLine){
				for (int k = i;k > 0;k --){
					this.BlockMap[k] = this.BlockMap[k - 1];
				}
				this.BlockMap[0] = new boolean[Tetris.BlockWidth];
				lines ++;
			}
		}
		return lines;
	}

	// 定时器监听
	ActionListener TimerListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO 自动生成的方法存根
			if (Tetris.this.IsTouch(Tetris.this.CurrBlockMap, new Point(Tetris.this.CurrBlockPos.x, Tetris.this.CurrBlockPos.y + 1))){
				if (Tetris.this.fixBlock()){
					Tetris.this.Score += Tetris.this.clearLines() * 10;
					Tetris.this.getNextBlock();
				}
				else{
					JOptionPane.showMessageDialog(Tetris.this.getParent(), "GAME OVER");
					Tetris.this.init();
				}
			}
			else{
				Tetris.this.CurrBlockPos.y ++;
			}
			Tetris.this.repaint();
		}
	};

	//按键监听
	java.awt.event.KeyListener KeyListener = new java.awt.event.KeyListener() {

		@Override
		public void keyPressed(KeyEvent e) {
			// TODO 自动生成的方法存根
			if (!IsPause) {
				Point DesPoint;
				switch (e.getKeyCode()) {
					case KeyEvent.VK_DOWN:
						DesPoint = new Point(Tetris.this.CurrBlockPos.x, Tetris.this.CurrBlockPos.y + 1);
						if (!Tetris.this.IsTouch(Tetris.this.CurrBlockMap, DesPoint)) {
							Tetris.this.CurrBlockPos = DesPoint;
						}
						break;
					case KeyEvent.VK_UP:
						boolean[][] TurnBlock = Tetris.this.rotateBlock(Tetris.this.CurrBlockMap, Math.PI / 2);
						if (!Tetris.this.IsTouch(TurnBlock, Tetris.this.CurrBlockPos)) {
							Tetris.this.CurrBlockMap = TurnBlock;
						}
						break;
					case KeyEvent.VK_RIGHT:
						DesPoint = new Point(Tetris.this.CurrBlockPos.x + 1, Tetris.this.CurrBlockPos.y);
						if (!Tetris.this.IsTouch(Tetris.this.CurrBlockMap, DesPoint)) {
							Tetris.this.CurrBlockPos = DesPoint;
						}
						break;
					case KeyEvent.VK_LEFT:
						DesPoint = new Point(Tetris.this.CurrBlockPos.x - 1, Tetris.this.CurrBlockPos.y);
						if (!Tetris.this.IsTouch(Tetris.this.CurrBlockMap, DesPoint)) {
							Tetris.this.CurrBlockPos = DesPoint;
						}
						break;
				}
				//System.out.println(Tetris.this.NowBlockPos);
				repaint();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO 自动生成的方法存根

		}

		@Override
		public void keyTyped(KeyEvent e) {
			// TODO 自动生成的方法存根
		}
	};
}// end class
