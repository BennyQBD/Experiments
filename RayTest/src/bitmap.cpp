#include "bitmap.h"
#include "stb_image.h"
#include <cstring>
#include <fstream>
#include <sstream>

Bitmap::Bitmap(const std::string& fileName)
{
	int numColorComponents;
	unsigned char* pixels = stbi_load(fileName.c_str(), (int*)&m_width, 
			(int*)&m_height, &numColorComponents, 4);
	
	m_image = new int[m_width * m_height];
	
	for(unsigned int i = 0; i < m_width * m_height; i++)
	{
		m_image[i] = (pixels[i*4 + 0] << 16) | (pixels[i*4 + 1] << 8) | pixels[i*4 + 2];
	}
	
	stbi_image_free(pixels);
}

Bitmap::Bitmap(unsigned int width, unsigned int height)
{
	m_image = new int[width * height];
	m_width = width;
	m_height = height;
	memset(m_image, 0, width*height);
}
	
Bitmap::~Bitmap()
{
	if(m_image) delete m_image;
}

static float sRGBEncode(float c)
{
	return c;
	//return powf(c, 1.0f/2.2f);
//	if(c < 0.0031308f)
//	{
//		return 12.92f * c;
//	}
//	else
//	{
//		// Inverse Gamma 2.4
//		return 1.055f * powf(c, 0.4166667f) - 0.055f;
//	}
}

static float Saturate(float c, float exposure)
{
	if(exposure == 0.0f)
	{
		return Clamp(c, 0.0f, 1.0f);
	}
	else
	{
		return 1.0f - expf(c * exposure * -1.0f);
	}
}

void Bitmap::DrawPixel(unsigned int x, unsigned int y, const Vector3f& color, float exposure)
{
	int r = ((int)(sRGBEncode(Saturate(color.GetX(), exposure)) * 255.0f + 0.5f)) & 0xFF;
	int g = ((int)(sRGBEncode(Saturate(color.GetY(), exposure)) * 255.0f + 0.5f)) & 0xFF;
	int b = ((int)(sRGBEncode(Saturate(color.GetZ(), exposure)) * 255.0f + 0.5f)) & 0xFF;
	
	unsigned int index = x + m_width * y;
	
	m_image[index] = (r << 16) | (g << 8) | b;
}

void Bitmap::Save(const std::string& fileName)
{
	std::ofstream output;
	output.open(fileName.c_str());
	
	std::ostringstream header;
	header << "P6 " << m_width << " " << m_height << " 255 ";	
	output << header.str();
	
	for(unsigned int j = 0; j < m_height; j++)
	{
		for(unsigned int i = 0; i < m_width; i++)
		{
			int currentPixel = m_image[i + j * m_width];
			
			unsigned char r = (unsigned char)((currentPixel >> 16) & 0xFF);
			unsigned char g = (unsigned char)((currentPixel >> 8) & 0xFF);
			unsigned char b = (unsigned char)((currentPixel >> 0) & 0xFF);
			
			output << r << g << b;
		}
	}
	
	output.close();
}
