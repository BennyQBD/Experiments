/*
 * Copyright (C) 2014 Benny Bobaganoosh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "transform.h"

void Transform::Rotate(const Quaternion& rotation)
{
	m_rot = Quaternion((rotation * m_rot).Normalized());
}

void Transform::LookAt(const Vector3f& point, const Vector3f& up)
{
	m_rot = GetLookAtRotation(point, up);
}

Matrix4f Transform::GetTransformation() const
{
	Matrix4f translationMatrix;
	Matrix4f scaleMatrix;

	translationMatrix.InitTranslation(Vector3f(m_pos.GetX(), m_pos.GetY(), m_pos.GetZ()));
	scaleMatrix.InitScale(Vector3f(m_scale, m_scale, m_scale));

	Matrix4f result = translationMatrix * m_rot.ToRotationMatrix() * scaleMatrix;

	return result;
}

// TODO: Implement properly
Quaternion Transform::GetTransformedRot() const
{	
	return m_rot;
}

Quaternion Transform::GetLookAtRotation(const Vector3f& point, const Vector3f& up) 
{ 
	return Quaternion(
			Matrix4f().InitRotationFromDirection(
				(point - m_pos).Normalized(), up)); 
}

