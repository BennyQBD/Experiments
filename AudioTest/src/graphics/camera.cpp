#include "camera.h"

Matrix4f Camera::GetViewProjection() const
{
	//This comes from the conjugate rotation because the world should appear to rotate
	//opposite to the camera's rotation.
	Matrix4f cameraRotation = m_transform->GetTransformedRot().Conjugate().ToRotationMatrix();
	Matrix4f cameraTranslation;
	
	//Similarly, the translation is inverted because the world appears to move opposite
	//to the camera's movement.
	cameraTranslation.InitTranslation(m_transform->GetTransformedPos() * -1);
	
	return m_projection * cameraRotation * cameraTranslation;
}

