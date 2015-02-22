#ifndef BITMAP_H
#define BITMAP_H

#include <string>
#include "math3d.h"

class Bitmap
{
public:
	Bitmap(const std::string& fileName);
	Bitmap(unsigned int width, unsigned int height);
	
	void Save(const std::string& fileName);
	void DrawPixel(unsigned int x, unsigned int y, const Vector3f& color, float exposure);

	inline unsigned int GetWidth() const { return m_width; }
	inline unsigned int GetHeight() const { return m_height; }
	inline const int* GetPixels() const { return m_image; }
	
	virtual ~Bitmap();
protected:
private:
	Bitmap(const Bitmap& other) { (void)other; }
	void operator=(const Bitmap& other) { (void)other; }

	int* m_image;
	unsigned int m_width;
	unsigned int m_height;
};

#endif
