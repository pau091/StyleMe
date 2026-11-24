/**
 * Adaptadores de salida.
 *
 * A quién llama StyleMe: persistencia (PostgreSQL con JPA), vector (pgvector),
 * gemini (embeddings y respuestas), seguridad (BCrypt, JWT), n8n (webhooks).
 * Cada adaptador implementa un puerto de salida.
 *
 * Ver docs/planeacion/16-arquitectura-hexagonal.md
 */
package co.styleme.infraestructura.adaptador.salida;
