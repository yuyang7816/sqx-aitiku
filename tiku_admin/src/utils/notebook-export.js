/**
 * 管理端错题导出：支持选择题目、学科、是否带答案与解析
 */

const escapeHtml = (value = '') => String(value)
  .replace(/&/g, '&amp;')
  .replace(/</g, '&lt;')
  .replace(/>/g, '&gt;')
  .replace(/"/g, '&quot;')

const wrapLines = (text = '', maxChars = 28) => {
  const source = String(text || '').trim()
  if (!source) {
    return ['—']
  }
  const lines = []
  let current = ''
  Array.from(source).forEach((char) => {
    if (current.length >= maxChars) {
      lines.push(current)
      current = char
      return
    }
    current += char
  })
  if (current) {
    lines.push(current)
  }
  return lines.length ? lines : ['—']
}

export function buildAdminNotebookExportPayload({
  items = [],
  subjects = [],
  includeAnswerAnalysis = true,
  userLabel = '',
} = {}) {
  const subjectSet = new Set((subjects || []).filter(Boolean))
  const filtered = (items || []).filter((item) => {
    if (!subjectSet.size) {
      return true
    }
    return subjectSet.has(item.subject)
  })

  return {
    userLabel: String(userLabel || '').trim(),
    subjects: subjectSet.size ? [...subjectSet] : ['全部学科'],
    includeAnswerAnalysis: Boolean(includeAnswerAnalysis),
    count: filtered.length,
    exportedAt: new Date().toLocaleString('zh-CN', { hour12: false }),
    items: filtered.map((item, index) => ({
      index: index + 1,
      subject: item.subject || '通用题目',
      question: item.title || item.question || '未命名题目',
      wrong: item.wrong || item.myAnswer || '—',
      right: item.right || item.rightAnswer || '—',
      analysis: item.analysis || '暂无详细解析',
    })),
  }
}

export function printAdminNotebookExport(payload = {}) {
  const includeAnswer = payload.includeAnswerAnalysis !== false
  const subjectLabel = Array.isArray(payload.subjects) ? payload.subjects.join('、') : '全部学科'
  const blocks = (payload.items || []).map((item) => {
    const questionHtml = wrapLines(item.question)
      .map((line) => `<p class="question-line">${escapeHtml(line)}</p>`)
      .join('')
    const answerHtml = includeAnswer
      ? `
        <p class="meta wrong">错误答案：${escapeHtml(item.wrong)}</p>
        <p class="meta right">正确答案：${escapeHtml(item.right)}</p>
        <h4>详细解析</h4>
        <div class="analysis">${wrapLines(item.analysis).map((line) => `<p class="analysis-line">${escapeHtml(line)}</p>`).join('')}</div>
      `
      : ''
    return `
      <section class="question-block">
        <h3>第 ${item.index} 题 · ${escapeHtml(item.subject)}</h3>
        <div class="question">${questionHtml}</div>
        ${answerHtml}
      </section>
    `
  }).join('')

  const modeLabel = includeAnswer ? '含答案与解析（错题分析）' : '纯题目（模拟考试）'
  const html = `<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8" />
<title>错题导出 · ${escapeHtml(subjectLabel)}</title>
<style>
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; color: #243041; padding: 24px; }
header { border-bottom: 2px solid #386df5; padding-bottom: 12px; margin-bottom: 24px; }
header h1 { margin: 0 0 8px; font-size: 22px; }
header p { margin: 0; color: #97a0af; font-size: 13px; }
.question-block { page-break-inside: avoid; margin-bottom: 28px; padding: 16px; border: 1px solid #e6ebf3; border-radius: 12px; }
.question-block h3 { margin: 0 0 12px; font-size: 15px; color: #386df5; }
.question-block h4 { margin: 16px 0 8px; font-size: 14px; color: #16a34a; }
.question-line { margin: 0 0 6px; font-size: 15px; line-height: 1.6; font-weight: 600; }
.analysis-line { margin: 0 0 6px; font-size: 14px; line-height: 1.7; color: #5d6777; }
.meta { margin: 6px 0; font-size: 13px; }
.meta.wrong { color: #f05963; }
.meta.right { color: #16a34a; }
.empty { color: #97a0af; }
</style>
</head>
<body>
<header>
  <h1>错题导出 · ${escapeHtml(subjectLabel)}</h1>
  <p>${escapeHtml(payload.userLabel || '用户错题')} · ${escapeHtml(modeLabel)} · 导出时间：${escapeHtml(payload.exportedAt || '')} · 共 ${payload.count || 0} 题</p>
</header>
${blocks || '<p class="empty">暂无选中题目</p>'}
</body>
</html>`

  const printWindow = window.open('', '_blank')
  if (!printWindow) {
    throw new Error('无法打开打印窗口，请允许弹窗')
  }
  printWindow.document.write(html)
  printWindow.document.close()
  printWindow.focus()
  setTimeout(() => {
    printWindow.print()
  }, 300)
}
