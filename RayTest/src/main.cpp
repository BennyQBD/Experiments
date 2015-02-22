//#include "display.h"
//#include "time.h"
//#include "physics.h"
#include "bitmap.h"
#include "math3d.h"
//#include "sobol.h"
//#include "mesh.h"

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

class Sphere
{
public:
	Sphere(const Vector3f& center, float radius, const Vector3f& diffuseColor,
			float reflectivity, float transparency, const Vector3f& emissionColor
			= Vector3f(0.0f, 0.0f, 0.0f)) :
		m_center(center),
		m_radius(radius),
		m_diffuseColor(diffuseColor),
		m_emissionColor(emissionColor),
		m_transparency(transparency),
		m_reflectivity(reflectivity) {}

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

	inline const Vector3f& GetCenter() const { return m_center; }
	inline const Vector3f& GetDiffuseColor() const { return m_diffuseColor; }
	inline const Vector3f& GetEmissionColor() const { return m_emissionColor; }
	inline float GetRadius() const { return m_radius; }
	inline float GetTransparency() const { return m_transparency; }
	inline float GetReflectivity() const { return m_reflectivity; }
private:
	Vector3f m_center;
	float m_radius;
	Vector3f m_diffuseColor;
	Vector3f m_emissionColor;
	float m_transparency;
	float m_reflectivity;
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
		if(sphere.GetEmissionColor().Length() != 0.0f)
		{
			m_spheres[SPHERE_TYPE_LIGHT].push_back(sphere);
		}
		else
		{
			m_spheres[SPHERE_TYPE_NORMAL].push_back(sphere);
		}
	}

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

	Vector3f CalculateDiffuseLighting(const Vector3f& p, const Vector3f& normal,
			const Sphere* sphere) const
	{
		Vector3f result(0.0f, 0.0f, 0.0f);
		for(size_t i = 0; i < m_spheres[SPHERE_TYPE_LIGHT].size(); i++)
		{
			const Sphere& currentSphere = m_spheres[SPHERE_TYPE_LIGHT][i];
			Vector3f lightDir = (currentSphere.GetCenter() - p).Normalized();
			
			NearestIntersection intersect = FindNearestIntersection(Ray(p, lightDir));

			if(intersect.sphere == NULL || intersect.sphere == &currentSphere)
			{
				float lightAmt = normal.Dot(lightDir);
				if(lightAmt < 0.0f)
				{
					lightAmt = 0.0f;
				}
				result += currentSphere.GetEmissionColor() * lightAmt;
			}
		}
		return result;
	}

	inline const Vector3f& GetBackgroundColor() const { return m_backgroundColor; }
	inline const Camera& GetCamera() const { return m_camera; }
	inline unsigned int GetMaxTraceDepth() const { return m_maxTraceDepth; }
	inline unsigned int GetSamples() const { return m_samples; }
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
};

static float Randf()
{
//	static int seed = 0;
//	float result = 0.0f;
//	i4_sobol(1, &seed, &result);
//	return result;

	return ((float)rand()/RAND_MAX);
}

static void QuasiRandf(float* res1, float* res2)
{
	static int seed = 0;
	float results[2];
	results[0] = Randf();
	results[1] = Randf();
//	i4_sobol(2, &seed, results);
	*res1 = results[0];
	*res2 = results[1];
}

static Vector3f Trace(const Ray& ray, const Scene& scene, const unsigned int depth)
{
	static const float BIAS = (float)1e-4;

	NearestIntersection intersect = scene.FindNearestIntersection(ray);
	float t = intersect.t;
	const Sphere* sphere = intersect.sphere;

	if(sphere == NULL)
	{
		return scene.GetBackgroundColor();
	}

	Vector3f hitLoc = ray.HitPoint(t);
	Vector3f normal = (hitLoc - sphere->GetCenter()).Normalized();
	bool inside = false;
	if(ray.GetDirection().Dot(normal) > 0)
	{
		normal = normal * -1;
		inside = true;
	}

	Vector3f surfaceColor(0.0f, 0.0f, 0.0f);
	Vector3f diffuseColor = sphere->GetDiffuseColor();
	float maxReflectance = diffuseColor.MaxComponent();

	if(maxReflectance < 1.0f/255.0f)
	{
		return sphere->GetEmissionColor();
	}

	unsigned int minRRdepth = (unsigned int)ceil(sqrt(scene.GetMaxTraceDepth()));
	if(depth >= minRRdepth)
	{
		// Russian Roulette termination
		if(Randf() < maxReflectance * 0.99f && 
				depth < scene.GetMaxTraceDepth())
		{
			diffuseColor = diffuseColor * (1.0f/maxReflectance);
		}
		else
		{
			return sphere->GetEmissionColor();
		}
	}

	float reflectance = sphere->GetReflectivity();
	float refractance = 0.0f;//sphere->GetTransparency();
	float diffuseRatio = 1.0f - reflectance;
	if(refractance != 0.0f)
	{
		diffuseRatio = 1.0f - refractance;
	}
	if(reflectance != 1.0f)
	{
//		float r1, r2;
//		QuasiRandf(&r1, &r2);
//		r1 *= 2.0f * (float)MATH_PI;
//		float sqrtr2 = sqrtf(r2);
//
//		Vector3f u = ((fabs(normal.GetX()) > 0.1f ? Vector3f(0.0f, 1.0f, 0.0f) :
//					Vector3f(1.0f, 0.0f, 0.0f)).Cross(normal));
//		Vector3f v = normal.Cross(u);
//
//		Vector3f newDirection = (u * cosf(r1) * sqrtr2 +
//				v * sinf(r1) * sqrtr2 + normal * sqrtf(1 - r2)).Normalized();
//
//		surfaceColor += diffuseColor.ComponentMultiply(
//				Trace(Ray(hitLoc + normal * BIAS, newDirection), 
//					scene, depth + 1)) * (1.0f - reflectance);
		Vector3f lightAmt = scene.CalculateDiffuseLighting(hitLoc, normal, sphere);
		surfaceColor += diffuseColor.ComponentMultiply(lightAmt)
			* (1.0f - reflectance);
	}
	if(reflectance != 0.0f)
	{
		float facingRatio = ray.GetDirection().Dot(normal) * -1;
		float oneMinusFacingRatio = 1 - facingRatio;
		float oneMinusFacingRatioCubed = oneMinusFacingRatio * 
			oneMinusFacingRatio * oneMinusFacingRatio;
		float fresnel = Mix(oneMinusFacingRatioCubed, 1.0f, 0.1f);
		
		Vector3f reflectDir = ray.GetDirection().Reflect(normal).Normalized();
		Ray reflectRay(hitLoc + normal * BIAS, reflectDir);
//		if(refractance != 0.0f)
//		{
//			float nc = 1.0f;
//			float nt = 1.5f;
//			float nnt = inside ? nc/nt : nt/nc;
//			float ddn = ray.GetDirection().Dot(normal);
//			float cos2t = 1 - nnt*nnt*(1-ddn*ddn);
//
//			if(cos2t < 0)
//			{
//				surfaceColor += diffuseColor.ComponentMultiply(
//						Trace(reflectRay, scene, depth + 1)) * refractance;
//			}
//			else
//			{
//				Vector3f tdir = (ray.GetDirection() * nnt - 
//						normal * (ddn * nnt + sqrtf(cos2t))).Normalized();
//				float a = nt - nc;
//				float b = nt + nc;
//				float R0 = a*a/(b*b);
//				float c = 1-(inside ? -ddn : tdir.Dot(normal));
//				
//				float Re = R0+(1-R0)*c*c*c*c*c;
//				float Tr = 1-Re;
//				float P = 0.25f + 0.5f * Re;
//				float RP = Re/P;
//				float TP = Tr/(1-P);
//
//				Vector3f refraction;
//				if(depth > minRRdepth)
//				{
//					if(Randf() < P)
//					{
//						refraction = Trace(reflectRay, scene, depth + 1) * RP;
//					}
//					else
//					{
//						refraction = Trace(Ray(hitLoc, tdir), scene, depth + 1) * TP;
//					}
//				}
//				else
//				{
//					refraction = Trace(reflectRay, scene, depth + 1) * Re
//						+ Trace(Ray(hitLoc, tdir), scene, depth + 1) * Tr;
//				}
//
//				surfaceColor += diffuseColor.ComponentMultiply(
//						refraction * (1.0f - fresnel)) * refractance;
//			}
//		}
//		else
		{	
			Vector3f reflection = Trace(reflectRay, scene, depth + 1);
			surfaceColor += diffuseColor.ComponentMultiply(
					reflection * fresnel) * reflectance;
		}
	}

	return surfaceColor + sphere->GetEmissionColor();

//	if((sphere->GetTransparency() > 0.0f || sphere->GetReflectivity() > 0.0f) 
//			&& depth < scene.GetMaxTraceDepth())
//	{
//		float facingRatio = ray.GetDirection().Dot(normal) * -1;
//		float oneMinusFacingRatio = 1 - facingRatio;
//		float oneMinusFacingRatioCubed = oneMinusFacingRatio * 
//			oneMinusFacingRatio * oneMinusFacingRatio;
//		float fresnel = Mix(oneMinusFacingRatioCubed, 1.0f, 0.1f);
//		
//		Vector3f reflectDir = ray.GetDirection().Reflect(normal).Normalized();
//		Vector3f reflection = Trace(Ray(hitLoc + normal * BIAS, reflectDir),
//				scene, depth + 1);
//
//		Vector3f refraction(0.0f, 0.0f, 0.0f);
//		if(sphere->GetTransparency() > 0.0f)
//		{
//			// TODO: Don't hardcode this
//			float ior = 1.1f;
//			float eta = inside ? ior : 1.0f / ior;
//			float cosi = normal.Dot(ray.GetDirection()) * -1;
//			float k = 1.0f - eta * eta * (1.0f - cosi * cosi);
//			
//			Vector3f refractionDir = (ray.GetDirection() * eta + 
//				normal * (eta * cosi - sqrtf(k))).Normalized();
//
//			refraction = Trace(Ray(hitLoc - normal * BIAS, refractionDir),
//					scene, depth + 1);
//		}
//		
//		surfaceColor += diffuseColor.ComponentMultiply(
//				reflection * fresnel +
//				refraction * (1.0f - fresnel) * sphere->GetTransparency());
//	}
//	else
//	{
//		float reflectivity = sphere->GetReflectivity() * 2.0f / 3.0f;
//		if(reflectivity > 0.0f && depth < scene.GetMaxTraceDepth())
//		{
//			Vector3f reflectDir = ray.GetDirection().Reflect(normal).Normalized();
//			Vector3f reflectAmt = Trace(Ray(Vector3f(hitLoc + normal * BIAS), 
//					reflectDir),
//					scene, depth + 1);
//
//			float phongTerm = reflectDir.Dot(ray.GetDirection() * -1);
//			
//			float specAmt = 8.0f;
//			float specVal = 1.0f;
//			phongTerm = powf(phongTerm, specAmt);
//			phongTerm = specVal * Clamp(phongTerm, 0.0f, 1.0f);
//			
//			surfaceColor += diffuseColor.ComponentMultiply(reflectAmt) 
//				* reflectivity;
//			//surfaceColor += diffuseColor * phongTerm * reflectivity / 2.0f;
//		}
//
//		if(reflectivity < 1.0f)
//		{
//			Vector3f lightAmt = scene.CalculateDiffuseLighting(hitLoc, normal, sphere);
//			surfaceColor += diffuseColor.ComponentMultiply(lightAmt)
//				* (1.0f - reflectivity);
//		}
//	}

//	return surfaceColor + sphere->GetEmissionColor();
}

static void Render(Bitmap* display, const Scene& scene)
{
	float sampleFactor = 1.0f/(float)scene.GetSamples();
	float sampleFactorSqrt = sqrtf(sampleFactor);
	
	float width = (float)display->GetWidth();
	float height = (float)display->GetHeight();
	float aspect = width/height;
	float invWidth = 1.0f/width;
	float invHeight = 1.0f/height;
	float fov = scene.GetCamera().fov;
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
					Ray viewRay(scene.GetCamera().pos, 
							Vector3f(x, y, -1).Normalized());
					color += Trace(viewRay, scene, 0) * sampleFactor;
				}
			}

			display->DrawPixel(i, j, color, scene.GetCamera().exposure);
		}
	}
}

int main()
{
	Bitmap result(320, 240);
	
	Camera camera;
	camera.pos = Vector3f(0,0,0);
	camera.fov = ToRadians(30.0f);
	camera.exposure = 0.0f;

	Scene scene(camera, Vector3f(1.0f, 1.0f, 1.0f), 5, 1);
	scene.AddSphere(Sphere(Vector3f(0.0f, -10004.0f, -20.0f), 10000.0f, 
				Vector3f(0.2f, 0.2f, 0.2f), 0.0f, 0.0f));
	scene.AddSphere(Sphere(Vector3f(0.0f, 0.0f, -20.0f), 4.0f,
				Vector3f(1.00f, 0.32f, 0.36f), 1.0f, 0.5f));
	scene.AddSphere(Sphere(Vector3f(5.0f, -1.0f, -15.0f), 2.0f,
				Vector3f(0.90f, 0.76f, 0.46f), 1.0f, 0.0f));
	scene.AddSphere(Sphere(Vector3f(5.0f, 0.0f, -25.0f), 3.0f,
				Vector3f(0.65f, 0.77f, 0.97f), 1.0f, 0.0f));
	scene.AddSphere(Sphere(Vector3f(-5.5f, 0.0f, -15.0f), 3.0f,
				Vector3f(0.90f, 0.90f, 0.90f), 1.0f, 0.0f));
	scene.AddSphere(Sphere(Vector3f(0.0f, 20.0f, -30.0f), 3.0f, 
				Vector3f(0.0f, 0.0f, 0.0f), 0.0f, 0.0f, Vector3f(3.0f, 3.0f, 3.0f)));


	Render(&result, scene);
	result.Save("./output.ppm");
    return 0;
}
