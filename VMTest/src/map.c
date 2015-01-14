#include "map.h"

#include <stdlib.h>
#include <string.h>
#include <assert.h>

static void map_node_create(struct map_node* self, void* key, void* value,
		const struct map* base);
static void map_node_release(struct map_node* self);

static void map_node_insert(struct map_node* self, void* key, void* value,
		const struct map* base);

static struct map_node* map_node_find(struct map_node* self, const void* key, 
		const struct map* base);

static void map_node_visit_prefix(struct map_node* self,
		void(*fn)(void* userdata, void* key, void* value),
		void* userdata);

static void map_node_visit_order(struct map_node* self,
		void(*fn)(void* userdata, void* key, void* value),
		void* userdata);


void map_create(struct map* self, size_t key_size, size_t value_size,
		int(*cmp_fn)(const void* a, const void*b))
{
	self->key_size = key_size;
	self->value_size = value_size;
	self->cmp_fn = cmp_fn;
	self->root = NULL;
}

void map_release(struct map* self)
{
	map_clear(self);
}

void map_insert(struct map* self, void* key, void* value)
{
	if(self->root == NULL) {
		self->root = (struct map_node*)malloc(sizeof(struct map_node));
		map_node_create(self->root, key, value, self);
	} else {
		map_node_insert(self->root, key, value, self);
	}
}

void* map_at(const struct map* self, const void* key)
{
	struct map_node* node;
	
	node = map_node_find(self->root, key, self);
	if(node == NULL) {
		return NULL;
	} else {
		return node->value;
	}
}

void map_visit_prefix(const struct map* self, 
		void* userdata,
		void(*fn)(void* userdata, void* key, void* value))
{
	map_node_visit_prefix(self->root, fn, userdata);
}

void map_visit_order(const struct map* self, 
		void* userdata,
		void(*fn)(void* userdata, void* key, void* value))
{
	map_node_visit_order(self->root, fn, userdata);
}

void map_clear(struct map* self)
{
	if(self->root == NULL) {
		return;
	}

	map_node_release(self->root);
	free(self->root);
	
	self->root = NULL;
}


static void map_node_create(struct map_node* self, void* key, void* value,
		const struct map* base)
{
	self->key = malloc(base->key_size);
	self->value = malloc(base->value_size);
	self->left = NULL;
	self->right = NULL;

	memcpy(self->key, key, base->key_size);
	memcpy(self->value, value, base->value_size);
}

static void map_node_release(struct map_node* self)
{
	free(self->key);
	free(self->value);

	if(self->left != NULL) {
		map_node_release(self->left);
		free(self->left);
	}
	
	if(self->right != NULL) {
		map_node_release(self->right);
		free(self->right);
	}
}

static void map_node_insert(struct map_node* self, void* key, void* value,
		const struct map* base)
{
	int cmp;

	cmp = base->cmp_fn(key, self->key);

	if(cmp > 0) {
		if(self->right == NULL) {
			self->right = (struct map_node*)malloc(sizeof(struct map_node));
			map_node_create(self->right, key, value, base);
		} else {
			map_node_insert(self->right, key, value, base);
		}
	} else if(cmp < 0) {
		if(self->left == NULL) {
			self->left = (struct map_node*)malloc(sizeof(struct map_node));
			map_node_create(self->left, key, value, base);
		} else {
			map_node_insert(self->left, key, value, base);
		}
	} else {
		self->value = value;
	}
}

static struct map_node* map_node_find(struct map_node* self, const void* key, 
		const struct map* base)
{
	int cmp;

	if(self == NULL) {
		return NULL;
	}

	cmp = base->cmp_fn(key, self->key);

	if(cmp > 0) {
		return map_node_find(self->right, key, base);
	} else if(cmp < 0) {
		return map_node_find(self->left, key, base);
	} else {
		return self;
	}
}

static void map_node_visit_prefix(struct map_node* self,
		void(*fn)(void* userdata, void* key, void* value),
		void* userdata)
{
	if(self == NULL) {
		return;
	}

	fn(userdata, self->key, self->value);
	map_node_visit_prefix(self->left, fn, userdata);
	map_node_visit_prefix(self->right, fn, userdata);
}

static void map_node_visit_order(struct map_node* self,
		void(*fn)(void* userdata, void* key, void* value),
		void* userdata)
{
	if(self == NULL) {
		return;
	}

	map_node_visit_order(self->left, fn, userdata);
	fn(userdata, self->key, self->value);
	map_node_visit_order(self->right, fn, userdata);
}



static int int_cmp(const void* a, const void* b)
{
	return *(int*)a - *(int*)b;
}

static void visit_print(void* userdata, void* key, void* value)
{
	int a = *(int*)key;
	int b = *(int*)value;

	(void)userdata;
	(void)a;
	(void)b;

	assert(b == (a * 2));
}

void map_unit_test()
{
	struct map m;
	int a;
	int b;

	map_create(&m, sizeof(int), sizeof(int), int_cmp);

	a = 1;
	b = 2;
	map_insert(&m, &a, &b);

	a = 3;
	b = 6;
	map_insert(&m, &a, &b);

	a = 2;
	b = 4;
	map_insert(&m, &a, &b);

	a = 5;
	b = 10;
	map_insert(&m, &a, &b);

	a = 4;
	b = 8;
	map_insert(&m, &a, &b);

	a = 3;
	b = *(int*)map_at(&m, &a);

	assert(b == 6);
	map_visit_order(&m, NULL, &visit_print);
	map_release(&m);
}

