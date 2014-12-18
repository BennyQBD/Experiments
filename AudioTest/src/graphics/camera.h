#ifndef CAMERA_INCLUDED_H
#define CAMERA_INCLUDED_H

#include "../core/transform.h"

class Camera
{
public:
	Camera(const Matrix4f& projection, Transform* transform) :
		m_projection(projection),
		m_transform(transform) {}
	
	Matrix4f GetViewProjection() const;

	inline const Transform& GetTransform() const { return *m_transform; }
protected:
private:
	Matrix4f   m_projection;
	Transform* m_transform;
};

#endif
