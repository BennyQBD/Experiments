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

#ifndef TRANSFORM_H
#define TRANSFORM_H

#include "math3d.h"

class Transform
{
public:
	Transform(const Vector3f& pos = Vector3f(0,0,0), const Quaternion& rot = Quaternion(0,0,0,1), float scale = 1.0f) :
		m_pos(pos),
		m_rot(rot),
		m_scale(scale) {}

	Matrix4f GetTransformation() const;
	inline void Move(const Vector3f& amt) { m_pos += amt; }
	void Rotate(const Quaternion& rotation);
	void LookAt(const Vector3f& point, const Vector3f& up);
	
	Quaternion GetLookAtRotation(const Vector3f& point, const Vector3f& up);

	inline const Vector3f& GetPos()       const { return m_pos; }
	inline const Quaternion& GetRot()     const { return m_rot; }
	inline float GetScale()               const { return m_scale; }
	
	//TODO: Implement these properly
	inline Vector3f GetTransformedPos()   const { return m_pos; }
	Quaternion GetTransformedRot()        const;

	inline void SetPos(const Vector3f& pos) { m_pos = pos; }
	inline void SetRot(const Quaternion& rot) { m_rot = rot; }
	inline void SetScale(float scale) { m_scale = scale; }
protected:
private:
	Vector3f m_pos;
	Quaternion m_rot;
	float m_scale;
};

#endif
