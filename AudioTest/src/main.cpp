#include <iostream>
#include "subsystem/sdl/sdlsubsystem.h"

//TODO: This is temporary include.
#include "stb_image.h"

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


	//BEGIN TEMP CODE
	int x, y, bytesPerPixel;
	unsigned char* data = stbi_load("./res/textures/bricks.jpg", &x, &y, &bytesPerPixel, 4);

	if(data == NULL)
	{
		std::cerr << "Unable to load texture: " << "./res/textures/bricks.jpg" << std::endl;
	}

	ITexture* texture = device->CreateTexture(x, y, data, 
			ITexture::FILTER_LINEAR_NO_MIPMAP, 0.0f, ITexture::FORMAT_RGBA,
		   	ITexture::FORMAT_RGBA, false);
	stbi_image_free(data);
	//END TEMP CODE

	
	RendererValues renderer;
	renderer.SetSamplerSlot("diffuse", 0);

	MaterialValues material;
	material.SetTexture("diffuse", texture);

	UniformData uniforms;
	uniforms.world = Matrix4f().InitIdentity();
	uniforms.viewProjection = Matrix4f().InitIdentity();
	uniforms.material = &material;
	uniforms.renderData = &renderer;

	IAudioContext* audioContext = subsystem->GetAudioContext();
	IAudioDevice* audioDevice = subsystem->GetAudioDevice();
	IAudioData* testSound = audioDevice->CreateAudioFromFile("./res/audio/testClip.wav", false);
	
	SampleInfo info;
	info.volume = 1.0f;
	
	AudioObject testSoundObject(testSound, info);
	audioContext->PlayAudio(testSoundObject);

	while(!display->IsClosed())
	{
		display->Update();

		context->ClearScreen(target, 0.0f, 0.0f, 0.0f, 0.0f);
		context->ClearDepth(target);
		context->DrawVertexArray(target, shader, vertexArray, uniforms);

		display->SwapBuffers();
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
 
