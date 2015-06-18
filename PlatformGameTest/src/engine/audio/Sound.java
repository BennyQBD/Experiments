package engine.audio;
public class Sound {
	private IAudioDevice device;
	private int soundId;

	public Sound(IAudioDevice device, SoundData data, double volume,
			double pitch, boolean shouldLoop) {
		this.device = device;
		this.soundId = device.createAudioObject(data.getId(), volume, pitch,
				shouldLoop);
	}
	
	@Override
	protected void finalize() throws Throwable {
		dispose();
		super.finalize();
	}

	public void dispose() {
		soundId = device.releaseAudioObject(soundId);
	}
	
	public void play() {
		device.play(soundId);
	}
	
	public void pause() {
		device.pause(soundId);
	}
	
	public void stop() {
		device.stop(soundId);
	}
}
