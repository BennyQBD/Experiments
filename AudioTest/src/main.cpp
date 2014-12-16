#include <iostream>
#include "subsystem/sdl/sdlsubsystem.h"
#include "core/transform.h"

#define SUBSYSTEM SDLSubSystem

int main(int argc, char** argv)
{
	(void)argc;
	(void)argv;

	ISubSystem* subsystem = new SUBSYSTEM();
	IDisplay* display = subsystem->CreateDisplay(800, 600, "My Display", false);
	IRenderContext* context = display->GetRenderContext();
	IRenderDevice* device = display->GetRenderDevice();
	
	IRenderTarget* target = display->GetRenderTarget();
	IShaderProgram* shader = 
		device->CreateShaderProgramFromFile("./res/shaders/basicShader.glsl");

	float positions[] =
	{
		-1.0f, -1.0f,  0.0f,
		 0.0f,  1.0f,  0.0f,
		 1.0f, -1.0f,  0.0f,
	};

	float texCoords[] =
	{
		0.0f, 0.0f,
		0.5f, 1.0f,
		1.0f, 0.0f,
	};
	unsigned int vertexElementSizes[] = { 3, 2 };
	unsigned int indices[] = { 0, 1, 2 };
	float* vertices[] = { positions, texCoords };

	IVertexArray* vertexArray = device->CreateVertexArray(
			vertices, vertexElementSizes, 2, 3, indices, 3);

	ITexture* texture = device->CreateTextureFromFile(
			"./res/textures/bricks.jpg", false,
			ITexture::FILTER_LINEAR_LINEAR_MIPMAP, 8.0f, false);
	
	RendererValues renderer;
	renderer.SetSamplerSlot("diffuse", 0);

	MaterialValues material;
	material.SetTexture("diffuse", texture);

	Transform transform;
	transform.SetPos(Vector3f(0, 0, 3.0f));

	UniformData uniforms;
	uniforms.world = transform.GetTransformation();
	uniforms.viewProjection = Matrix4f().InitPerspective(
			ToRadians(70.0f), 800.0f/600.0f, 0.1f, 1000.0f);
	uniforms.material = &material;
	uniforms.renderData = &renderer;

	IAudioContext* audioContext = subsystem->GetAudioContext();
	IAudioDevice* audioDevice = subsystem->GetAudioDevice();
	IAudioData* testSound = 
		audioDevice->CreateAudioFromFile("./res/audio/testClip.wav", false);
	
	SampleInfo info;
	info.volume = 1.0f;
	
	AudioObject testSoundObject(testSound, &info);
	audioContext->PlayAudio(testSoundObject);

	float angle = 0.0f;
	while(!display->IsClosed())
	{
		display->Update();

		context->ClearScreen(target, 0.0f, 0.0f, 0.0f, 0.0f);
		context->ClearDepth(target);
		context->DrawVertexArray(target, shader, vertexArray, uniforms);

		display->SwapBuffers();
		transform.SetRot(Quaternion());
		transform.Rotate(Quaternion(Vector3f(1, 0, 0), angle));
		transform.Rotate(Quaternion(Vector3f(0, 1, 0), angle));
		transform.Rotate(Quaternion(Vector3f(0, 0, 1), angle));
		uniforms.world = transform.GetTransformation();
		angle += 0.015f;
	}

	audioContext->StopAudio(testSoundObject);
	audioDevice->ReleaseAudio(testSound);

	device->ReleaseTexture(texture);
	device->ReleaseVertexArray(vertexArray);
	device->ReleaseShaderProgram(shader);
	subsystem->ReleaseDisplay(display);
	delete subsystem;
	return 0;
}
 
