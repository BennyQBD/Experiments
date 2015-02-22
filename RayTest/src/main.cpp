#include "bitmap.h"
#include "math3d.h"

#include <iostream>
#include <float.h>
#include <vector>

struct Camera
{
	Vector3f pos;
	float fov;
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

	Vector3f GetDirectionFromPoint(const Vector3f& point) const
	{
		return (m_center - point).Normalized();
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
	Scene(const Camera& camera, const Vector3f& backgroundColor, 
			unsigned int maxTraceDepth, unsigned int samples) :
		m_camera(camera),
		m_backgroundColor(backgroundColor),
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
						Ray viewRay(m_camera.pos, 
								Vector3f(x, y, -1).Normalized());
						color += Trace(viewRay, 0) * sampleFactor;
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
	Vector3f m_backgroundColor;
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
			Vector3f lightDir = currentSphere.GetDirectionFromPoint(p);

			NearestIntersection intersect = FindNearestIntersection(Ray(p, lightDir));

			if(intersect.sphere == NULL || intersect.sphere == &currentSphere)
			{
				float lightAmt = normal.Dot(lightDir);
				if(lightAmt < 0.0f)
				{
					lightAmt = 0.0f;
				}
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
			return m_backgroundColor;
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

		if(depth >= m_maxTraceDepth || maxReflectance < 1.0f/255.0f)
		{
			return material.GetEmissionColor();
		}

		float reflectRatio = sphere->GetMaterial().GetReflectivity();
		float diffuseRatio = 1.0f - reflectRatio;

		if(diffuseRatio != 0.0f)
		{
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
	Bitmap result(800, 600);
	
	Camera camera;
	camera.pos = Vector3f(0,0,0);
	camera.fov = ToRadians(30.0f);
	camera.exposure = 0.0f;

	Scene scene(camera, Vector3f(1.0f, 1.0f, 1.0f), 5, 1);
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
