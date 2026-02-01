package si.um.feri.copycats;
import com.badlogic.gdx.Game;

import si.um.feri.copycats.screen.IntroScreen;

public class App extends Game{
    @Override
    public void create(){
        setScreen(new IntroScreen(this));
    }

    @Override
    public void dispose(){
        super.dispose();
    }
}
