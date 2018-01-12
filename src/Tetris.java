import javax.swing.*;
import java.awt.*;


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

    }

    public void setMode(String mode) {
        if(mode.equals("v4")) {
            Tetris.Shapes = Block.Shapes;
        } else {
            // do nothing
        }
        this.Init();
        this.repaint();
    }

    // when the new block is falling
    private void getNewBlock() {
        // iterator
        this.CurrBlockMap = this.NextBlockMap;
        this.CurrBlockState = this.NextBlockState;
        // get next state
        this.NextBlockState = this.createNewBlockState();
        this.NextBlockMap = this.getBlockMap(this.NextBlockMap);
        // caculate the pos of the block
        this.CurrBlockPos = this.calNewBolckInitPos();
    }

    // if the falling block touch the falled blocks;
    private boolean IsTouch(boolean[][] SrcNextBlockMap,Point SrcNextBlockPos) {
        
    }

}
