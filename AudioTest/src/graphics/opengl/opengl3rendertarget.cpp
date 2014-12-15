#include "opengl3rendertarget.h"
#include <GL/glew.h>

void OpenGL3RenderTarget::Bind()
{
	glBindTexture(GL_TEXTURE_2D,0);
	glBindFramebuffer(GL_FRAMEBUFFER, m_index);
	
	glViewport(0, 0, m_width, m_height);
}
