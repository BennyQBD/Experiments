#include "bitmap.h"
#include "math3d.h"
#include "sobol.h"

#include <iostream>
#include <float.h>
#include <vector>

struct Camera
{
	Vector3f pos;
	float fov;
	float depthOfField;
	float exposure;
};

class Ray
{
public:
	Ray(const Vector3f& origin, const Vector3f& direction) :
		m_origin(origin),
		m_direction(direction.Normalized()) {}

	Vector3f HitPoint(float t) const { return m_origin + m_direction * t; }

	inline const Vector3f& GetOrigin() const { return m_origin; }
	inline const Vector3f& GetDirection() const { return m_direction; }
private:
	Vector3f m_origin;
	Vector3f m_direction;
};

class Cubemap : public Bitmap
{
public:
	Cubemap(const std::string& fileName) : Bitmap(fileName) {}

	Vector3f ReadPixel(const Ray& ray) const 
	{
		static const unsigned int CUBE_UP = 0;
		static const unsigned int CUBE_DOWN = 1;

		static const unsigned int CUBE_RIGHT = 4;
		static const unsigned int CUBE_LEFT = 5;
		static const unsigned int CUBE_FORWARD = 2;
		static const unsigned int CUBE_BACKWARD = 3;

		const Vector3f rayDir = ray.GetDirection();
		float ardx = fabsf(rayDir.GetX());
		float ardy = fabsf(rayDir.GetY());
		float ardz = fabsf(rayDir.GetZ());

//		return GetCubemapPixel(CUBE_RIGHT, 
//				1.0f - (rayDir.GetZ()/rayDir.GetX() + 1.0f) * 0.5f,
//				(rayDir.GetY()/rayDir.GetX() + 1.0f) * 0.5f);

		if(ardx >= ardy && ardx >= ardz)
		{
			if(rayDir.GetX() > 0.0f)
			{
				return GetCubemapPixel(CUBE_RIGHT,
						1.0f - (rayDir.GetY()/rayDir.GetX() + 1.0f) * 0.5f,
						1.0f - (rayDir.GetZ()/rayDir.GetX() + 1.0f) * 0.5f);
			}
			else
			{
				return GetCubemapPixel(CUBE_LEFT,
						(rayDir.GetY()/rayDir.GetX() + 1.0f) * 0.5f,
						1.0f - (rayDir.GetZ()/rayDir.GetX() + 1.0f) * 0.5f); 
			}
		}
		else if(ardy >= ardx && ardy >= ardz)
		{
			if(rayDir.GetY() > 0.0f)
			{
				return GetCubemapPixel(CUBE_UP,
						(rayDir.GetX()/rayDir.GetY() + 1.0f) * 0.5f,
						1.0f - (rayDir.GetZ()/rayDir.GetY() + 1.0f) * 0.5f);
			}
			else
			{
				return GetCubemapPixel(CUBE_DOWN,
						1.0f - (rayDir.GetX()/rayDir.GetY() + 1.0f) * 0.5f,
						(rayDir.GetZ()/rayDir.GetY() + 1.0f) * 0.5f); 
			}
		}
		else if(ardz >= ardx && ardz >= ardy)
		{
			if(rayDir.GetZ() > 0.0f)
			{
				return GetCubemapPixel(CUBE_FORWARD,
						1.0f - (rayDir.GetY()/rayDir.GetZ() + 1.0f) * 0.5f,
						(rayDir.GetX()/rayDir.GetZ() + 1.0f) * 0.5f);
			}
			else
			{
				return GetCubemapPixel(CUBE_BACKWARD,
						(rayDir.GetY()/rayDir.GetZ() + 1.0f) * 0.5f,
						(rayDir.GetX()/rayDir.GetZ() + 1.0f) * 0.5f); 
			}
		}

		return Vector3f(0.0f, 0.0f, 0.0f);
	}
private:
	Vector3f GetPixel(unsigned int faceNum, unsigned int x, unsigned int y) const
	{
		x += GetHeight() * faceNum;
		const int* pixels = GetPixels();

		int pixel = pixels[x + GetWidth() * y];

		float r = ((pixel >> 16) & 0xFF)/255.0f;
		float g = ((pixel >> 8) & 0xFF)/255.0f;
		float b = ((pixel) & 0xFF)/255.0f;

		return Vector3f(r, g, b);
	}

	Vector3f GetCubemapPixel(unsigned int faceNum, float xIn, float yIn) const
	{
		if(faceNum >= 6)
		{
			// TODO: Possibly different error handling?
			return Vector3f(0.0f, 0.0f, 0.0f);
		}
	
		// Despite the name, this is supposed to be GetHeight
		unsigned int width = GetHeight();
		unsigned int height = GetHeight();

//		unsigned int x = (unsigned int)(xIn * width + 0.5f);
//		unsigned int y = (unsigned int)(yIn * height + 0.5f);
//
//		return GetPixel(faceNum, x, y);

		float u = fabsf(xIn);
		float v = fabsf(yIn);
		unsigned int uMin = (unsigned int)(width * u);
		unsigned int vMin = (unsigned int)(height * v);
		unsigned int uMax = (unsigned int)(width * u) + 1;
		unsigned int vMax = (unsigned int)(height * v) + 1;

		float ucoef = fabsf(width * u - uMin);
		float vcoef = fabsf(height * v - vMin);

		uMin = Clamp(uMin, 0u, width - 1);
		uMax = Clamp(uMax, 0u, width - 1);
		vMin = Clamp(vMin, 0u, height - 1);
		vMax = Clamp(vMax, 0u, height - 1);

		Vector3f samp1 = GetPixel(faceNum, uMin, vMin);
		Vector3f samp2 = GetPixel(faceNum, uMax, vMin);
		Vector3f samp3 = GetPixel(faceNum, uMin, vMax);
		Vector3f samp4 = GetPixel(faceNum, uMax, vMax);

		return 
			(samp1 * (1.0f - ucoef) + samp2 * ucoef) * (1.0f - vcoef) +
			(samp3 * (1.0f - ucoef) + samp4 * ucoef) * vcoef;

	}
};


class Material
{
public:
	Material(const Vector3f& diffuseColor, float reflectivity, 
			const Vector3f& emissionColor = Vector3f(0.0f, 0.0f, 0.0f)) :
		m_diffuseColor(diffuseColor),
		m_emissionColor(emissionColor),
		m_reflectivity(reflectivity) {}

	inline const Vector3f& GetDiffuseColor() const { return m_diffuseColor; }
	inline const Vector3f& GetEmissionColor() const { return m_emissionColor; }
	inline float GetReflectivity() const { return m_reflectivity; }
private:
	Vector3f m_diffuseColor;
	Vector3f m_emissionColor;
	float m_reflectivity;
};

static float Randf()
{
	return (float)rand()/RAND_MAX;
}

static void QuasiRand2f(float* res1, float* res2)
{
	static int seed = 0;
	float res[2];
	i4_sobol(2, &seed, res);
	*res1 = res[0];
	*res2 = res[1];
//	*res1 = Randf();
//	*res2 = Randf();
}

class Sphere
{
public:
	Sphere(const Vector3f& center, float radius, const Material& material) :
		m_center(center),
		m_radius(radius),
		m_material(material) {}
	
	float IntersectRay(const Ray& ray) const
	{
		Vector3f dist = m_center - ray.GetOrigin();
		float B = ray.GetDirection().Dot(dist);
		float D = B*B - dist.Dot(dist) + m_radius * m_radius;
		if(D < 0.0f)
		{
			return FLT_MAX;
		}

		float t0 = B - sqrtf(D);
		float t1 = B + sqrtf(D);
		
		float t = FLT_MAX;
		if((t0 > 0.1f) && (t0 < t))
		{
			t = t0;
		}
		if((t1 > 0.1f) && (t1 < t))
		{
			t = t1;
		}

		return t;
	}

	Vector3f GetNormal(const Vector3f& surfaceLoc) const
	{
		return (surfaceLoc - m_center).Normalized();
	}

	Vector3f GetRandomDirectionFromPoint(const Vector3f& point) const
	{
		float r1, r2;
//		r1 = Randf();
//		r2 = Randf();
		QuasiRand2f(&r1, &r2);
		r1 *= 2.0f * (float)MATH_PI;
		r2 = acosf(2.0f * r2 - 1.0f);

		Vector3f randomDir(
				cosf(r1)*sinf(r2),
				sinf(r1)*sinf(r2),
				cosf(r2));

		return ((randomDir.Normalized() * m_radius + m_center) - point).Normalized();
	}

	inline const Material& GetMaterial() const { return m_material; }
private:
	Vector3f m_center;
	float m_radius;
	Material m_material;
};

struct NearestIntersection
{
	float t;
	const Sphere* sphere;
};


class Scene
{
public:
	Scene(const Camera& camera, const Cubemap& background, unsigned int minTraceDepth,
			unsigned int maxTraceDepth, unsigned int samples) :
		m_camera(camera),
		m_background(&background),
		m_minTraceDepth(minTraceDepth),
		m_maxTraceDepth(maxTraceDepth),
		m_samples(samples) {}
	void AddSphere(const Sphere& sphere) 
	{
		if(sphere.GetMaterial().GetEmissionColor().Length() != 0.0f)
		{
			m_spheres[SPHERE_TYPE_LIGHT].push_back(sphere);
		}
		else
		{
			m_spheres[SPHERE_TYPE_NORMAL].push_back(sphere);
		}
	}

	void Render(Bitmap* display) const
	{
		float sampleFactor = 1.0f/(float)m_samples;
		float sampleFactorSqrt = sqrtf(sampleFactor);
		
		float width = (float)display->GetWidth();
		float height = (float)display->GetHeight();
		float aspect = width/height;
		float invWidth = 1.0f/width;
		float invHeight = 1.0f/height;
		float fov = m_camera.fov;
		float tanHalfFOV = (float)tan(0.5*fov);
		
		for(unsigned int j = 0; j < display->GetHeight(); j++)
		{
			for(unsigned int i = 0; i < display->GetWidth(); i++)	
			{
				Vector3f color(0.0f, 0.0f, 0.0f);

				for(float jj = (float)j; jj < (float)(j + 1); jj += sampleFactorSqrt)
				{
					for(float ii = (float)i; ii < (float)(i + 1); ii += sampleFactorSqrt)
					{
						float x = (2 * ((ii + sampleFactorSqrt / 2.0f) * invWidth) - 1)
							* tanHalfFOV * aspect;
						float y = (1 - 2 * ((jj + sampleFactorSqrt / 2.0f) * invHeight))
							* tanHalfFOV;

						Vector3f origin = m_camera.pos;
						Vector3f direction = Vector3f(x, y, -1).Normalized();

						if(m_camera.depthOfField != 0.0f)
						{
							float r1, r2;
							r1 = Randf();
							r2 = Randf();
							//QuasiRand2f(&r1, &r2);
							Vector3f disturbance(
									m_camera.depthOfField * r1,
									m_camera.depthOfField * r2, 0.0f);

							Vector3f aimedPoint = origin + direction;
							origin = origin + disturbance;
							direction = (aimedPoint - origin).Normalized();
						}

						color += Trace(Ray(origin, direction), 0) * sampleFactor;
					}
				}

				display->DrawPixel(i, j, color, m_camera.exposure);
			}
		}
	}
private:
	enum
	{
		SPHERE_TYPE_NORMAL,
		SPHERE_TYPE_LIGHT,

		SPHERE_TYPE_SIZE
	};
	std::vector<Sphere> m_spheres[SPHERE_TYPE_SIZE];
	Camera m_camera;
	const Cubemap* m_background;
	unsigned int m_minTraceDepth;
	unsigned int m_maxTraceDepth;
	unsigned int m_samples;

	NearestIntersection FindNearestIntersection(const Ray& ray) const
	{
		float tNear = FLT_MAX;
		const Sphere* sphere = NULL;
		for(size_t j = 0; j < SPHERE_TYPE_SIZE; j++)
		{
			for(size_t i = 0; i < m_spheres[j].size(); i++)
			{
				float tCurrent = m_spheres[j][i].IntersectRay(ray);
				if(tCurrent < tNear)
				{
					tNear = tCurrent;
					sphere = &m_spheres[j][i];
				}
			}
		}

		NearestIntersection result;
		result.t = tNear;
		result.sphere = sphere;
		return result;
	}

	Vector3f CalculateDiffuseLighting(const Vector3f& p, const Vector3f& normal) const
	{
		Vector3f result(0.0f, 0.0f, 0.0f);
		for(size_t i = 0; i < m_spheres[SPHERE_TYPE_LIGHT].size(); i++)
		{
			const Sphere& currentSphere = m_spheres[SPHERE_TYPE_LIGHT][i];
			Vector3f lightDir = currentSphere.GetRandomDirectionFromPoint(p);
			NearestIntersection intersect = FindNearestIntersection(Ray(p, lightDir));

			if(intersect.sphere == NULL || intersect.sphere == &currentSphere)
			{
				float lightAmt = normal.Dot(lightDir);
				result += currentSphere.GetMaterial().GetEmissionColor() * lightAmt;
			}
		}
		return result;
	}

	Vector3f Trace(const Ray& ray, const unsigned int depthIn) const
	{
		static const float BIAS = (float)1e-4;
		const unsigned int depth = depthIn + 1;

		NearestIntersection intersect = FindNearestIntersection(ray);
		float t = intersect.t;
		const Sphere* sphere = intersect.sphere;

		if(sphere == NULL)
		{
			return m_background->ReadPixel(ray);
		}

		Vector3f hitLoc = ray.HitPoint(t);
		Vector3f normal = sphere->GetNormal(hitLoc);
		const Material& material = sphere->GetMaterial();
		if(ray.GetDirection().Dot(normal) > 0)
		{
			normal = normal * -1;
		}

		Vector3f surfaceColor(0.0f, 0.0f, 0.0f);
		Vector3f diffuseColor = material.GetDiffuseColor();
		float maxReflectance = diffuseColor.MaxComponent();

		if(depth > m_maxTraceDepth || maxReflectance < 1.0f/255.0f)
		{
			return material.GetEmissionColor();
		}
		else if(depth > m_minTraceDepth)
		{
			if(Randf() < maxReflectance)
			{
				diffuseColor = diffuseColor * (1.0f/maxReflectance);
			}
			else
			{
				return material.GetEmissionColor();
			}
		}

		float reflectRatio = sphere->GetMaterial().GetReflectivity();
		float diffuseRatio = 1.0f - reflectRatio;

		if(diffuseRatio != 0.0f)
		{
//			float r1, r2;
//			QuasiRand2f(&r1, &r2);
//			r1 *= 2.0f * (float)MATH_PI;
//			r2 = acosf(2.0f * r2 - 1.0f);
//
//			Vector3f newDirection(
//					cosf(r1)*sinf(r2),
//					sinf(r1)*sinf(r2),
//					cosf(r2));
//
//			if(newDirection.Dot(normal) < 0)
//			{
//				newDirection = newDirection * -1.0f;
//			}
//			float r1 = Randf();
//			float r2 = Randf();
//			r1 *= 2.0f * (float)MATH_PI;
//			float sqrtr2 = sqrtf(r2);
//
//			Vector3f u = ((fabs(normal.GetX()) > 0.1f ? Vector3f(0.0f, 1.0f, 0.0f) :
//						Vector3f(1.0f, 0.0f, 0.0f)).Cross(normal));
//			Vector3f v = normal.Cross(u);
//
//			Vector3f newDirection = (u * cosf(r1) * sqrtr2 +
//					v * sinf(r1) * sqrtr2 + normal * sqrtf(1 - r2)).Normalized();
//
//			surfaceColor += diffuseColor.ComponentMultiply(
//					Trace(Ray(hitLoc + normal * BIAS, newDirection), 
//						depth)) * diffuseRatio;

			Vector3f lightAmt = CalculateDiffuseLighting(hitLoc, normal);
			surfaceColor += diffuseColor.ComponentMultiply(lightAmt)
				* diffuseRatio;
		}
		if(reflectRatio != 0.0f)
		{
			float facingRatio = ray.GetDirection().Dot(normal) * -1;
			float oneMinusFacingRatio = 1 - facingRatio;
			float oneMinusFacingRatioCubed = oneMinusFacingRatio * 
				oneMinusFacingRatio * oneMinusFacingRatio;
			float fresnel = Mix(oneMinusFacingRatioCubed, 1.0f, 0.1f);
			
			Vector3f reflectDir = ray.GetDirection().Reflect(normal).Normalized();
			Ray reflectRay(hitLoc + normal * BIAS, reflectDir);

			Vector3f reflection = Trace(reflectRay, depth);
			surfaceColor += diffuseColor.ComponentMultiply(
					reflection * fresnel) * reflectRatio;
		}

		return surfaceColor + material.GetEmissionColor();
	}
};


int main()
{
	Bitmap result(320, 240);
	
	Camera camera;
	camera.pos = Vector3f(0,0,0);
	camera.fov = ToRadians(30.0f);
	camera.exposure = 1.0f;
	camera.depthOfField = 0.0f/50.0f;

	Cubemap background("./res/envmap.png");

	Scene scene(camera, background, 3, 8, 16);
	scene.AddSphere(Sphere(Vector3f(0.0f, -10004.0f, -20.0f), 10000.0f, 
				Material(Vector3f(0.2f, 0.2f, 0.2f), 0.0f)));
	scene.AddSphere(Sphere(Vector3f(0.0f, 0.0f, -20.0f), 4.0f,
				Material(Vector3f(1.00f, 0.32f, 0.36f), 1.0f)));
	scene.AddSphere(Sphere(Vector3f(5.0f, -1.0f, -15.0f), 2.0f,
				Material(Vector3f(0.90f, 0.76f, 0.46f), 1.0f)));
	scene.AddSphere(Sphere(Vector3f(5.0f, 0.0f, -25.0f), 3.0f,
				Material(Vector3f(0.65f, 0.77f, 0.97f), 1.0f)));
	scene.AddSphere(Sphere(Vector3f(-5.5f, 0.0f, -15.0f), 3.0f,
				Material(Vector3f(0.90f, 0.90f, 0.90f), 1.0f)));
	scene.AddSphere(Sphere(Vector3f(0.0f, 20.0f, -30.0f), 3.0f, 
				Material(Vector3f(0.0f, 0.0f, 0.0f), 0.0f, Vector3f(3.0f, 3.0f, 3.0f))));

	scene.Render(&result);
	result.Save("./output.ppm");
    return 0;
}
