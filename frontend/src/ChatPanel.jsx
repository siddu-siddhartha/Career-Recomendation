import { useEffect, useRef, useState } from 'react'

const SUGGESTIONS = [
  'What role fits my strengths?',
  'What skill should I learn next?',
  'How does my quiz affect my matches?',
]

export default function ChatPanel({ onSend }) {
  const [messages, setMessages] = useState([
    { role: 'model', content: 'Hi, I’m your career guide. Ask me about your matches, skill gaps, quiz results, or next steps.', intro: true },
  ])
  const [input, setInput] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const listRef = useRef(null)

  useEffect(() => {
    listRef.current?.scrollTo({ top: listRef.current.scrollHeight, behavior: 'smooth' })
  }, [messages, busy])

  async function submit(event) {
    event.preventDefault()
    const content = input.trim()
    if (!content || busy) return
    const nextMessages = [...messages, { role: 'user', content }].slice(-12)
    setMessages(nextMessages)
    setInput('')
    setError('')
    setBusy(true)
    try {
      const result = await onSend(nextMessages)
      setMessages([...nextMessages, { role: 'model', content: result.answer, mode: result.mode }])
    } catch (problem) {
      setError(problem.message)
    } finally {
      setBusy(false)
    }
  }

  function resetChat() {
    setMessages([{ role: 'model', content: 'Hi, I’m your career guide. Ask me about your matches, skill gaps, quiz results, or next steps.', intro: true }])
    setInput('')
    setError('')
  }

  return (
    <section className="content-page chat-page">
      <div className="page-heading chat-heading"><div><span className="eyebrow">NORTHSTAR CAREER GUIDE</span><h1>Ask about your next step</h1><p>Get help interpreting your matches and planning a skill-building path.</p></div><button className="button button-outline" type="button" onClick={resetChat} title="Start a new conversation"><span aria-hidden="true">↺</span> New chat</button></div>
      <div className="chat-surface">
        <div className="chat-toolbar"><span className="chat-status-dot" />{messages.some((message) => message.mode === 'gemini') ? 'Gemini AI guide' : 'Career guide'}<span className="chat-toolbar-note">Career guidance only</span></div>
        <div className="chat-messages" ref={listRef} aria-live="polite">{messages.map((message, index) => <article className={`chat-message ${message.role === 'user' ? 'from-user' : 'from-guide'}`} key={`${index}-${message.role}`}><span className="chat-avatar">{message.role === 'user' ? 'Y' : 'N'}</span><div className="chat-bubble"><p>{message.content}</p>{message.role === 'model' && message.mode && <span className="reply-mode">{message.mode === 'gemini' ? 'AI response' : 'Local guide'}</span>}</div></article>)}{busy && <div className="chat-thinking"><span /><span /><span />Thinking through your question</div>}</div>
        {!messages.some((message) => message.role === 'user') && <div className="chat-suggestions">{SUGGESTIONS.map((suggestion) => <button type="button" key={suggestion} onClick={() => setInput(suggestion)}>{suggestion}<span>↗</span></button>)}</div>}
        {error && <p className="chat-error" role="alert">{error}</p>}
        <form className="chat-composer" onSubmit={submit}><textarea value={input} onChange={(event) => setInput(event.target.value)} onKeyDown={(event) => { if (event.key === 'Enter' && !event.shiftKey) { event.preventDefault(); event.currentTarget.form.requestSubmit() } }} placeholder="Ask a career question…" aria-label="Message your career guide" rows="2" maxLength="1500" /><button type="submit" className="send-button" disabled={busy || !input.trim()} aria-label="Send message" title="Send message">↑</button><span className="composer-hint">Enter to send · Shift+Enter for a new line</span></form>
      </div>
      <p className="chat-privacy-note">Don’t include passwords or private identifiers. If Gemini is enabled, your question and career profile context are sent to Google. Career guidance is informational, not a guarantee of employment.</p>
    </section>
  )
}
