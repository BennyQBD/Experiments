#include <iostream>
#include "subsystem/sdl/sdlsubsystem.h"
#include "core/transform.h"
#include "core/coreEngine.h"

#include "basicRenderer.h"
#include "basicScene.h"
#include "components/meshrenderer.h"
#include "components/basicsound.h"
#include "audio/sinWave.h"
#include <cstring>

#define SUBSYSTEM SDLSubSystem

class MyBasicScene : public BasicScene
{
public:
	virtual void Init(ResourceManager* resources, float aspect)
	{
		Mesh mesh(resources->GetMesh("./res/models/terrain02.obj"));
		Texture texture(resources->GetTexture("./res/textures/bricks.jpg", true,
			ITexture::FILTER_LINEAR_NEAREST_MIPMAP, 0.0f, false));

		MaterialValues* values = new MaterialValues();
		values->SetTexture("diffuse", texture);

		Material material = resources->RegisterMaterial("greyBricks", values);

		AudioData data = 
			resources->RegisterAudioData("sinWaveTest", new SinWave(440.0));
			//resources->GetAudioData("./res/audio/sample.wav", false);
		SampleInfo info;
		memset(&info, 0, sizeof(info));
		info.volume = -0.65;
		info.pitchAdjust = 0.0;
		info.loopStart = 0.0;
		info.loopEnd = 0.5;
//		info.loopStart = 0.2;
//		info.loopEnd = 0.95;
		AudioObject audio(data, info);

		Add((new Entity())->Add(new MeshRenderer(mesh, material)));
		Add((new Entity())->Add(new BasicSound(audio)));
	}
};

int main(int argc, char** argv)
{
	(void)argc;
	(void)argv;

	ISubSystem* subsystem = new SUBSYSTEM();
	IDisplay* display = subsystem->CreateDisplay(800, 600, "My Display", false);
	IRenderDevice* device = display->GetRenderDevice();
	
	IRenderTarget* target = display->GetRenderTarget();
	IShaderProgram* shader = 
		device->CreateShaderProgramFromFile("./res/shaders/basicShader.glsl");
	
	RendererValues renderVals;
	renderVals.SetSamplerSlot("diffuse", 0);

	Transform transform;
	transform.SetPos(Vector3f(0, 0, 3.0f));

	Transform cameraTransform;
	Camera camera(Matrix4f().InitPerspective(
			ToRadians(70.0f), (float)display->GetWidth()/(float)display->GetHeight(),
			0.1f, 1000.0f),
		   	&cameraTransform);

	
	ITimingSystem* timingSystem = subsystem->GetTimingSystem();
	IRenderer* renderer = new BasicRenderer(display->GetRenderContext(), 
			target, shader, &camera, &renderVals);
	IScene* scene = new MyBasicScene();
	

	IAudioContext* audioContext = subsystem->GetAudioContext();
	IAudioDevice* audioDevice = subsystem->GetAudioDevice();

	CoreEngine engine(60.0f, display, audioContext, audioDevice, timingSystem,
		   	renderer, scene);
	engine.Start();

	
	delete scene;
	delete renderer;

	device->ReleaseShaderProgram(shader);
	subsystem->ReleaseDisplay(display);
	delete subsystem;
	return 0;
}
 
