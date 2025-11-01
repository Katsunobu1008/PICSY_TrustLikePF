// frontend-vue/src/lib/errorMessages.js
// 役割: バックエンドから返るエラーコードや可否理由コードを人間が読める文言に変換する

const ERROR_MESSAGES = {
  ACCOUNT_FROZEN: 'あなたのアカウントは凍結中のため操作できません。',
  TARGET_FROZEN: 'この投稿の作者は現在凍結されています。',
  INSUFFICIENT_PURCHASING_POWER: '購買力が不足しています。',
  SELF_LIKE_NOT_ALLOWED: '自分の投稿に Like はできません。',
  POST_NOT_FOUND: '対象の投稿が見つかりませんでした。',
  ORIGINAL_POST_NOT_FOUND: '元の投稿が見つかりませんでした。',
  ORIGINAL_POST_ID_MISSING: '元投稿の情報が不足しています。',
  ORIGINAL_ROYALTY_NOT_SET: 'ロイヤリティ設定が未設定のため処理できません。',
  INVALID_ROYALTY_RATE: 'ロイヤリティ率の設定が不正です。',
  INVALID_DEFAULT_BETA: 'システム設定のβ値が無効です。管理者へ連絡してください。',
  INVALID_BETA_OVERRIDE: '指定されたβ値が不正です。',
  ACTOR_NOT_FOUND: '指定されたアクターが存在しません。',
  POST_CREATOR_NOT_FOUND: '投稿者が存在しません。',
  CONTRIBUTION_VECTOR_MISSING:
    '購買力の情報が取得できませんでした。しばらくしてから再度お試しください。',
  BAD_REQUEST: 'リクエスト内容に誤りがあります。',
  REQUEST_ID_CONFLICT: '同じリクエストが既に処理されています。最新の状態を確認してください。',
}

const REASON_MESSAGES = {
  NO_ACTOR: 'アクターを設定してください。',
  INSUFFICIENT_PURCHASING_POWER: ERROR_MESSAGES.INSUFFICIENT_PURCHASING_POWER,
  INSUFFICIENT_POWER: ERROR_MESSAGES.INSUFFICIENT_PURCHASING_POWER,
  SELF_LIKE_NOT_ALLOWED: ERROR_MESSAGES.SELF_LIKE_NOT_ALLOWED,
  ACCOUNT_FROZEN: ERROR_MESSAGES.ACCOUNT_FROZEN,
  TARGET_FROZEN: ERROR_MESSAGES.TARGET_FROZEN,
  FETCH_FAILED: '状態の取得に失敗しました。ページを更新するか後ほど再試行してください。',
}

export function affordanceReasonMessage(code) {
  if (!code || code === 'OK') return ''
  return REASON_MESSAGES[code] || ERROR_MESSAGES[code] || code
}

export function resolveApiError(error, fallback = '処理に失敗しました。') {
  const code = error?.response?.data?.code
  if (code && ERROR_MESSAGES[code]) return ERROR_MESSAGES[code]

  const message = error?.response?.data?.message
  if (message) {
    if (ERROR_MESSAGES[message]) return ERROR_MESSAGES[message]
    return message
  }

  if (error?.message) {
    if (/timeout/i.test(error.message)) {
      return 'タイムアウトしました。通信状態を確認し、再度お試しください。'
    }
    return error.message
  }

  return fallback
}

export const actionLabel = {
  like: 'Like',
  quote: 'Quote',
}
