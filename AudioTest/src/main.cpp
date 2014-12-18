#include <iostream>
#include "subsystem/sdl/sdlsubsystem.h"
#include "core/transform.h"
#include "core/coreEngine.h"

#include "basicRenderer.h"
#include "basicScene.h"
#include "components/meshrenderer.h"

#define SUBSYSTEM SDLSubSystem

class MyBasicScene : public BasicScene
{
public:
	virtual void Init(IRenderDevice* device, float aspect)
	{
		m_device = device;
		m_mesh = device->CreateVertexArrayFromFile("./res/models/terrain02.obj");
		m_texture = device->CreateTextureFromFile(
			"./res/textures/bricks.jpg", true,
			ITexture::FILTER_LINEAR_NEAREST_MIPMAP, 0.0f, false);

		m_material = new MaterialValues();
		m_material->SetTexture("diffuse", m_texture);
		
		Add((new Entity())->Add(new MeshRenderer(m_mesh, m_material)));
	}

	virtual ~MyBasicScene()
	{
		delete m_material;
		m_device->ReleaseTexture(m_texture);
		m_device->ReleaseVertexArray(m_mesh);
	}
private:
	IRenderDevice* m_device;
	IVertexArray* m_mesh;
	ITexture* m_texture;
	MaterialValues* m_material;
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
	
	CoreEngine engine(60.0f, display, timingSystem, renderer, scene);
	engine.Start();

	delete scene;
	delete renderer;

	device->ReleaseShaderProgram(shader);
	subsystem->ReleaseDisplay(display);
	delete subsystem;
	return 0;
}

/*
   IAudioContext* audioContext = subsystem->GetAudioContext();
	IAudioDevice* audioDevice = subsystem->GetAudioDevice();
	IAudioData* testSound = 
		audioDevice->CreateAudioFromFile("./res/audio/testClip.wav", true);
	
	SampleInfo info;
	info.volume = 1.0f;
	
	AudioObject testSoundObject(testSound, &info);
	audioContext->PlayAudio(testSoundObject);

	audioContext->StopAudio(testSoundObject);
	audioDevice->ReleaseAudio(testSound);

   */
 
