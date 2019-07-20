import org.json.JSONObject;

import java.awt.MouseInfo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

import javax.swing.JOptionPane;

public class ScreenHiderMain {

	private ScreenHiderWindow w;
	private static boolean muteSound = false;
	private boolean isActive = false;

	public static void main(String[] args) {
		String kasutusjuhend = "See programm katab ekraani kinni (justkui lülitab ekraani välja) kohe, kui toa uks avatakse (uks on vähem kinni kui 98%).\n\n"
				+ "Kui oled musta akna peale klikkinud (aken on fookuses), saab kirjutada klaviatuuril käske. \nKäsud on:\n"
				+ "1. 'exit' - paneb programmi täielikult kinni\n"
				+ "2. 'change' - vahetab ekraani, mis kinni kaetakse\n"
				+ "3. 'hide' - peidab musta kasti ära, nii, et ekraani on võimalik näha. Peale selle käsu kirjutamist on programm aktiveeritud ja ootab ukse avanemist.\n\n"
				+ "Kui aken on suletud ja on vaja seda esile tõsta, et sisestada käskusid, tuleb hoida hiirt üleval vasakus nurgas 10 sekundit. Siis tuleb programmi aken jälle nähtavale.\n\n"
				+ "Kas soovid ka vaigistada arvutist tuleva heli, kui ekraan peidetakse?";
		int answer = JOptionPane.showConfirmDialog(null, kasutusjuhend, "Programmi kasutusjuhend", JOptionPane.YES_NO_OPTION);
		muteSound = answer==JOptionPane.YES_OPTION;
		new ScreenHiderMain();
	}

	public ScreenHiderMain() {
		w = new ScreenHiderWindow(this);
	}

	public void start(){
		isActive = true;
		w.hideScreen(muteSound);
		new Thread(() -> {
			int mouseCounter = 0;
			while(isActive){
				if(MouseInfo.getPointerInfo().getLocation().x == 0  &&  MouseInfo.getPointerInfo().getLocation().y == 0){
					mouseCounter++;
					if(mouseCounter == 100){
						w.hideScreen();
					}
				}else{
					mouseCounter = 0;
				}
				double uks = uksetase();
				if(uks == -1){
					//võisid tekkida probleemid internetiga, proovin uuesti
					System.out.println("Probleemid netiga #1");
					uks = uksetase();
					if(uks == -1){
						//endiselt probleeme, proovin veel viimast korda
						System.out.println("Probleemid netiga #2");
						uks = uksetase();
						if(uks == -1){
							//nüüd on suht kindlalt server maas
							//annan märku, et tekkis viga, ekraan kaetakse ka igaksjuhuks kinni
							System.out.println("Probleemid netiga #3 => Server on maas!");
							JOptionPane.showMessageDialog(null, "Paistab, et server on maas.\nProgramm ei saa nii edasi töötada!", "Tekkis viga => server on maas!", JOptionPane.ERROR_MESSAGE);
							System.exit(0);
						}
					}
				}
				if(uks < 0.98){
					w.hideScreen(muteSound  &&  w.isScreenShown());
				}
				try { Thread.sleep(100); } catch (InterruptedException ignored) {}
			}
			w.showScreen();
		}).start();
	}

	public void stop(){
		isActive = false;
	}

	public void onShutdown(){
		if(w != null) {
			w.onShutdown();
		}
	}

	public boolean isActive(){
		return isActive;
	}

	/** @return ukse kinnisus: 1 = 100% kinni, 0 = 0% kinni (lahti), -1 kui ei õnnestu ukse taset leida */
	public double uksetase(){
		double ans = -1;
		InputStream is = null;
		try {
			is = new URL("http://192.168.1.81/kk/get").openStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			StringBuilder sb = new StringBuilder();
			int cp;
			while ((cp = rd.read()) != -1){
				sb.append((char) cp);
			}
			JSONObject obj = new JSONObject(sb.toString());

			int doorLevel = obj.getInt("door_level");
			int min = 50;
			int max = 1888;
			ans = (doorLevel-min)/(double)(max-min);
			ans = Math.max(ans, 0);
			ans = Math.min(ans, 1);
			ans = 1 - ans;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {e.printStackTrace();}
		}
		return ans;
	}
}
