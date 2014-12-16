#ifndef INDEXED_MODEL_INCLUDED_H
#define INDEXED_MODEL_INCLUDED_H

#include "../core/math3d.h"

#include <vector>

class IndexedModel
{
public:
	IndexedModel() {}
	IndexedModel(const std::vector<unsigned int> indices, const std::vector<Vector3f>& positions, const std::vector<Vector2f>& texCoords,
		const std::vector<Vector3f>& normals = std::vector<Vector3f>(), const std::vector<Vector3f>& tangents = std::vector<Vector3f>()) :
			m_indices(indices),
			m_positions(positions),
			m_texCoords(texCoords),
			m_normals(normals),
			m_tangents(tangents) {}

	bool IsValid() const;
	void CalcNormals();
	void CalcTangents();

	void AddVertex(const Vector3f& vert);
	inline void AddVertex(float x, float y, float z) { AddVertex(Vector3f(x, y, z)); }
	
	void AddTexCoord(const Vector2f& texCoord);
	inline void AddTexCoord(float x, float y) { AddTexCoord(Vector2f(x, y)); }
	
	void AddNormal(const Vector3f& normal);
	inline void AddNormal(float x, float y, float z) { AddNormal(Vector3f(x, y, z)); }
	
	void AddTangent(const Vector3f& tangent);
	inline void AddTangent(float x, float y, float z) { AddTangent(Vector3f(x, y, z)); }
	
	void AddFace(unsigned int vertIndex0, unsigned int vertIndex1, unsigned int vertIndex2);

	inline bool HasTexCoords() const { return m_texCoords.size() != 0; }
	inline bool HasNormals()   const { return m_normals.size() != 0; }
	inline bool HasTangents()  const { return m_tangents.size() != 0; }

	inline const std::vector<unsigned int>& GetIndices() const { return m_indices; }
	inline const std::vector<Vector3f>& GetPositions()   const { return m_positions; }
	inline const std::vector<Vector2f>& GetTexCoords()   const { return m_texCoords; }
	inline const std::vector<Vector3f>& GetNormals()     const { return m_normals; }
	inline const std::vector<Vector3f>& GetTangents()    const { return m_tangents; }
private:
	std::vector<unsigned int> m_indices;
    std::vector<Vector3f> m_positions;
    std::vector<Vector2f> m_texCoords;
    std::vector<Vector3f> m_normals;
    std::vector<Vector3f> m_tangents;  
};

#endif
