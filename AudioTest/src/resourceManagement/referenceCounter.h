#ifndef REFERENCE_COUNTER_INCLUDED_H
#define REFERENCE_COUNTER_INCLUDED_H

class ReferenceCounter
{
public:
	ReferenceCounter() :
		m_refCount(1) {}
	
	inline int GetReferenceCount() { return m_refCount; }
	
	inline void AddReference() { m_refCount++; }
	inline bool RemoveReference() { m_refCount--; return m_refCount == 0; }
protected:
private:
	int m_refCount;
};

#endif
