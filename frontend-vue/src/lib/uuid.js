// frontend-vue/src/lib/uuid.js
// 役割: requestId 生成ユーティリティ
export function newRequestId() {
  if (crypto?.randomUUID) return crypto.randomUUID()
  // Fallback（ほぼ使われないはず）
  return 'req-' + Math.random().toString(36).slice(2) + Date.now().toString(36)
}
