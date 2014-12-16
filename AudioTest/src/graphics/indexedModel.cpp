#include "indexedModel.h"

bool IndexedModel::IsValid() const
{
	return 
		(!HasTexCoords() || (m_positions.size() == m_texCoords.size())) &&
		(!HasNormals()   || (m_positions.size() == m_normals.size())) &&
		(!HasTangents()  || (m_positions.size() == m_tangents.size()));
}

void IndexedModel::AddVertex(const Vector3f& vert)
{
	m_positions.push_back(vert);
}

void IndexedModel::AddTexCoord(const Vector2f& texCoord)
{
	m_texCoords.push_back(texCoord);
}

void IndexedModel::AddNormal(const Vector3f& normal)
{
	m_normals.push_back(normal);
}
	
void IndexedModel::AddTangent(const Vector3f& tangent)
{
	m_tangents.push_back(tangent);
}

void IndexedModel::AddFace(unsigned int vertIndex0, unsigned int vertIndex1, unsigned int vertIndex2)
{
	m_indices.push_back(vertIndex0);
	m_indices.push_back(vertIndex1);
	m_indices.push_back(vertIndex2);
}

void IndexedModel::CalcNormals()
{
	m_normals.clear();
	m_normals.reserve(m_positions.size());
	
	for(unsigned int i = 0; i < m_positions.size(); i++)
	{
		m_normals.push_back(Vector3f(0,0,0));
	}

	for(unsigned int i = 0; i < m_indices.size(); i += 3)
	{
		unsigned int i0 = m_indices[i];
		unsigned int i1 = m_indices[i + 1];
		unsigned int i2 = m_indices[i + 2];
			
		Vector3f v1 = m_positions[i1] - m_positions[i0];
		Vector3f v2 = m_positions[i2] - m_positions[i0];
		
		Vector3f normal = v1.Cross(v2).Normalized();
		
		m_normals[i0] = m_normals[i0] + normal;
		m_normals[i1] = m_normals[i1] + normal;
		m_normals[i2] = m_normals[i2] + normal;
	}
	
	for(unsigned int i = 0; i < m_normals.size(); i++)
	{
		m_normals[i] = m_normals[i].Normalized();
	}
}

void IndexedModel::CalcTangents()
{
	m_tangents.clear();
	m_tangents.reserve(m_positions.size());
	
	for(unsigned int i = 0; i < m_positions.size(); i++)
	{
		m_tangents.push_back(Vector3f(0,0,0));
	}
		
	for(unsigned int i = 0; i < m_indices.size(); i += 3)
    {
		unsigned int i0 = m_indices[i];
		unsigned int i1 = m_indices[i + 1];
		unsigned int i2 = m_indices[i + 2];
    
        Vector3f edge1 = m_positions[i1] - m_positions[i0];
        Vector3f edge2 = m_positions[i2] - m_positions[i0];
        
        float deltaU1 = m_texCoords[i1].GetX() - m_texCoords[i0].GetX();
        float deltaU2 = m_texCoords[i2].GetX() - m_texCoords[i0].GetX();
        float deltaV1 = m_texCoords[i1].GetY() - m_texCoords[i0].GetY();
        float deltaV2 = m_texCoords[i2].GetY() - m_texCoords[i0].GetY();
        
        float dividend = (deltaU1 * deltaV2 - deltaU2 * deltaV1);
        float f = dividend == 0.0f ? 0.0f : 1.0f/dividend;
        
        Vector3f tangent = Vector3f(0,0,0);
        
        tangent.SetX(f * (deltaV2 * edge1.GetX() - deltaV1 * edge2.GetX()));
        tangent.SetY(f * (deltaV2 * edge1.GetY() - deltaV1 * edge2.GetY()));
        tangent.SetZ(f * (deltaV2 * edge1.GetZ() - deltaV1 * edge2.GetZ()));

//Bitangent example, in Java
//		Vector3f bitangent = new Vector3f(0,0,0);
//		
//		bitangent.setX(f * (-deltaU2 * edge1.getX() - deltaU1 * edge2.getX()));
//		bitangent.setX(f * (-deltaU2 * edge1.getY() - deltaU1 * edge2.getY()));
//		bitangent.setX(f * (-deltaU2 * edge1.getZ() - deltaU1 * edge2.getZ()));

		m_tangents[i0] += tangent;
		m_tangents[i1] += tangent;
		m_tangents[i2] += tangent;	
    }

    for(unsigned int i = 0; i < m_tangents.size(); i++)
	{
		m_tangents[i] = m_tangents[i].Normalized();
	}
}

