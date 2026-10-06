import { useEffect, useState } from 'react'
import ChatPanel from './ChatPanel.jsx'
import CareerQuiz from './CareerQuiz.jsx'
import { ApplicationTrackerView, CareerToolkitView, RoadmapView, RoleCompareView } from './CareerFeatures.jsx'
import './App.css'

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const PROFICIENCIES = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT']

async function request(path, { token, ...options } = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  })
  if (!response.ok) {
    const body = await response.json().catch(() => ({}))
    const problem = new Error(body.detail || body.message || `Request failed (${response.status})`)
    problem.status = response.status
    throw problem
  }
  return response.status === 204 ? null : response.json()
}

function AuthScreen() {
  const [mode, setMode] = useState('signup')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function submit(event) {
    event.preventDefault()
    setError('')
    setBusy(true)
    try {
      const result = await request(`/api/auth/${mode}`, {
        method: 'POST',
        body: JSON.stringify({ email, password }),
      })
      localStorage.setItem('career-token', result.token)
      localStorage.setItem('career-email', result.profile.email)
      window.location.reload()
    } catch (problem) {
      setError(problem.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-panel">
        <a className="brand brand-light" href="#home" aria-label="Northstar home"><span className="brand-mark">N</span><span>northstar<span className="brand-period">.</span></span></a>
        <div className="auth-copy">
          <span className="eyebrow">A clearer next step</span>
          <h1>Build a career<br />that fits <em>you.</em></h1>
          <p>See where your strengths can take you, then make a practical plan to get there.</p>
          <div className="auth-stats"><span><strong>01</strong> Your strengths</span><span><strong>02</strong> Your next role</span><span><strong>03</strong> Your next steps</span></div>
        </div>
        <div className="auth-orbit" aria-hidden="true"><span>career<br />compass</span></div>
        <p className="auth-footnote">Thoughtful recommendations. Built around your profile.</p>
      </section>
      <section className="auth-form-side">
        <div className="auth-form-wrap">
          <div className="auth-mobile-brand"><span className="brand-mark">N</span> northstar<span className="brand-period">.</span></div>
          <span className="eyebrow">{mode === 'signup' ? 'Start with you' : 'Welcome back'}</span>
          <h2>{mode === 'signup' ? 'Find your direction.' : 'Pick up where you left off.'}</h2>
          <p className="auth-subtitle">{mode === 'signup' ? 'Create an account to get your personalized career map.' : 'Sign in to revisit your career recommendations.'}</p>
          <form className="auth-form" onSubmit={submit}>
            <label>Email address<input type="email" autoComplete="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="you@example.com" required /></label>
            <label>Password<input type="password" autoComplete={mode === 'signup' ? 'new-password' : 'current-password'} value={password} onChange={(event) => setPassword(event.target.value)} placeholder="At least 8 characters" minLength={mode === 'signup' ? 8 : undefined} required /></label>
            {error && <p className="form-error" role="alert">{error}</p>}
            <button className="button button-dark button-wide" type="submit" disabled={busy}>{busy ? 'One moment…' : mode === 'signup' ? 'Create my account' : 'Sign in'}<span aria-hidden="true">↗</span></button>
          </form>
          <p className="auth-switch">{mode === 'signup' ? 'Already have an account?' : 'New to northstar?'} <button type="button" onClick={() => { setMode(mode === 'signup' ? 'login' : 'signup'); setError('') }}>{mode === 'signup' ? 'Sign in' : 'Create an account'}</button></p>
          <p className="auth-note">Your profile stays yours. We use it only to shape your recommendations.</p>
        </div>
      </section>
    </main>
  )
}

function ProfileForm({ profile, onSave, saving }) {
  const [form, setForm] = useState(() => ({
    education: profile.education || '',
    experienceYears: profile.experienceYears ?? 0,
    workStyle: profile.workStyle || '',
    preferredIndustry: profile.preferredIndustry || '',
    academicScore: profile.academicScore ?? '',
    assessmentScores: profile.assessmentScores || {},
    skills: profile.skills || [],
  }))
  const [newSkill, setNewSkill] = useState('')
  const [interestsText, setInterestsText] = useState((profile.interests || []).join(', '))

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }))
  }

  function addSkill(event) {
    event.preventDefault()
    const name = newSkill.trim()
    if (!name || form.skills.some((skill) => skill.name.toLowerCase() === name.toLowerCase())) return
    update('skills', [...form.skills, { name, proficiency: 'INTERMEDIATE' }])
    setNewSkill('')
  }

  function submit(event) {
    event.preventDefault()
    onSave({
      ...form,
      experienceYears: Number(form.experienceYears),
      academicScore: form.academicScore === '' ? null : Number(form.academicScore),
      interests: interestsText.split(',').map((item) => item.trim()).filter(Boolean),
    })
  }

  return (
    <form className="profile-form" onSubmit={submit}>
      <div className="form-grid">
        <label>Education level<select value={form.education} onChange={(event) => update('education', event.target.value)} required><option value="">Choose your level</option><option>High school</option><option>Associate</option><option>Bachelor</option><option>Master</option><option>Doctorate</option></select></label>
        <label>Years of experience<input type="number" min="0" max="60" value={form.experienceYears} onChange={(event) => update('experienceYears', event.target.value)} /></label>
        <label>Preferred industry<input value={form.preferredIndustry} onChange={(event) => update('preferredIndustry', event.target.value)} placeholder="e.g. Technology" /></label>
        <label>Work style<select value={form.workStyle} onChange={(event) => update('workStyle', event.target.value)}><option value="">Choose a work style</option><option>Collaborative</option><option>Independent</option><option>Flexible</option><option>Structured</option></select></label>
        <label>Academic marks <span className="label-hint">Percentage, 0–100</span><input type="number" min="0" max="100" step="1" value={form.academicScore} onChange={(event) => update('academicScore', event.target.value)} placeholder="e.g. 82" /></label>
      </div>
      <label className="field-label">Skills</label>
      <div className="skill-editor">
        {form.skills.map((skill) => <div className="skill-edit-row" key={skill.name}><span>{skill.name}</span><select aria-label={`${skill.name} proficiency`} value={skill.proficiency} onChange={(event) => update('skills', form.skills.map((item) => item.name === skill.name ? { ...item, proficiency: event.target.value } : item))}>{PROFICIENCIES.map((level) => <option key={level} value={level}>{level.charAt(0) + level.slice(1).toLowerCase()}</option>)}</select><button className="remove-skill" type="button" aria-label={`Remove ${skill.name}`} onClick={() => update('skills', form.skills.filter((item) => item.name !== skill.name))}>×</button></div>)}
        <div className="add-skill-row"><input value={newSkill} onChange={(event) => setNewSkill(event.target.value)} placeholder="Add a skill, e.g. Python" /><button className="button button-outline button-small" onClick={addSkill} type="button">+ Add skill</button></div>
      </div>
      <label>Interests <span className="label-hint">Separate with commas</span><input value={interestsText} onChange={(event) => setInterestsText(event.target.value)} placeholder="e.g. design, research, problem solving" /></label>
      <div className="form-actions"><span className="form-hint">Your recommendations update when you save.</span><button className="button button-dark" type="submit" disabled={saving}>{saving ? 'Saving…' : 'Save profile'}<span aria-hidden="true">↗</span></button></div>
    </form>
  )
}

function App() {
  const [token, setToken] = useState(() => localStorage.getItem('career-token'))
  const [profile, setProfile] = useState(null)
  const [matches, setMatches] = useState([])
  const [history, setHistory] = useState([])
  const [activeView, setActiveView] = useState('overview')
  const [selectedRole, setSelectedRole] = useState(null)
  const [roleDetail, setRoleDetail] = useState(null)
  const [filter, setFilter] = useState('')
  const [loading, setLoading] = useState(Boolean(token))
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [toast, setToast] = useState('')

  useEffect(() => {
    if (!token) return
    let cancelled = false
    async function load() {
      setLoading(true)
      try {
        const [nextProfile, nextMatches] = await Promise.all([request('/api/profile', { token }), request('/api/recommendations', { token })])
        if (!cancelled) {
          setProfile(nextProfile)
          setMatches(nextMatches)
          setError('')
        }
      } catch (problem) {
        if (!cancelled) {
          if (problem.status === 401 || problem.status === 403) signOut()
          else setError(problem.message)
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    load()
    return () => { cancelled = true }
  }, [token])

  function signOut() {
    localStorage.removeItem('career-token')
    localStorage.removeItem('career-email')
    setToken(null)
    setProfile(null)
    setMatches([])
    setHistory([])
    setSelectedRole(null)
    setRoleDetail(null)
  }

  async function saveProfile(values) {
    setSaving(true)
    setError('')
    try {
      const saved = await request('/api/profile', { token, method: 'PUT', body: JSON.stringify(values) })
      setProfile(saved)
      setMatches(await request('/api/recommendations', { token }))
      setActiveView('overview')
      setToast('Your profile and recommendations are up to date.')
      window.setTimeout(() => setToast(''), 3500)
    } catch (problem) {
      setError(problem.message)
    } finally {
      setSaving(false)
    }
  }

  async function saveQuiz(scores) {
    if (!profile) return
    await saveProfile({
      education: profile.education,
      experienceYears: profile.experienceYears,
      workStyle: profile.workStyle,
      preferredIndustry: profile.preferredIndustry,
      skills: profile.skills || [],
      interests: profile.interests || [],
      academicScore: profile.academicScore,
      assessmentScores: scores,
    })
  }

  async function sendChat(messages) {
    const conversation = messages
      .filter((message) => !message.intro)
      .map(({ role, content }) => ({ role, content }))
    return request('/api/chat', { token, method: 'POST', body: JSON.stringify({ messages: conversation }) })
  }

  async function openRole(match) {
    setSelectedRole(match)
    setRoleDetail(null)
    try {
      setRoleDetail(await request(`/api/recommendations/roles/${match.roleId}`, { token }))
    } catch (problem) {
      setError(problem.message)
    }
  }

  async function openHistory() {
    setActiveView('history')
    try {
      setHistory(await request('/api/recommendations/history', { token }))
    } catch (problem) {
      setError(problem.message)
    }
  }

  if (!token) return <AuthScreen />

  const email = profile?.email || localStorage.getItem('career-email') || 'there'
  const firstName = email.split('@')[0].split(/[._-]/)[0]
  const filteredMatches = matches.filter((match) => `${match.role} ${match.industry}`.toLowerCase().includes(filter.toLowerCase()))
  const profileReady = Boolean(profile?.education)

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <a className="brand" href="#overview" onClick={() => setActiveView('overview')}><span className="brand-mark">N</span><span>northstar<span className="brand-period">.</span></span></a>
        <div className="nav-label">WORKSPACE</div>
        <nav className="main-nav" aria-label="Main navigation">
          <button className={activeView === 'overview' ? 'nav-item active' : 'nav-item'} aria-label="Overview" title="Overview" onClick={() => setActiveView('overview')}><span className="nav-icon">⌂</span>Overview</button>
          <button className={activeView === 'profile' ? 'nav-item active' : 'nav-item'} aria-label="My profile" title="My profile" onClick={() => setActiveView('profile')}><span className="nav-icon">◉</span>My profile</button>
          <button className={activeView === 'quiz' ? 'nav-item active' : 'nav-item'} aria-label="Career quiz" title="Career quiz" onClick={() => setActiveView('quiz')}><span className="nav-icon">◇</span>Career quiz</button>
          <button className={activeView === 'compare' ? 'nav-item active' : 'nav-item'} aria-label="Compare roles" title="Compare roles" onClick={() => setActiveView('compare')}><span className="nav-icon">↔</span>Compare roles</button>
          <button className={activeView === 'roadmap' ? 'nav-item active' : 'nav-item'} aria-label="Action plan" title="Action plan" onClick={() => setActiveView('roadmap')}><span className="nav-icon">✓</span>Action plan</button>
          <button className={activeView === 'toolkit' ? 'nav-item active' : 'nav-item'} aria-label="Career toolkit" title="Career toolkit" onClick={() => setActiveView('toolkit')}><span className="nav-icon">⌕</span>Career toolkit</button>
          <button className={activeView === 'applications' ? 'nav-item active' : 'nav-item'} aria-label="Applications" title="Applications" onClick={() => setActiveView('applications')}><span className="nav-icon">▤</span>Applications</button>
          <button className={activeView === 'chat' ? 'nav-item active' : 'nav-item'} aria-label="Career guide" title="Career guide" onClick={() => setActiveView('chat')}><span className="nav-icon">✉</span>Career guide</button>
          <button className={activeView === 'history' ? 'nav-item active' : 'nav-item'} aria-label="History" title="History" onClick={openHistory}><span className="nav-icon">◷</span>History</button>
        </nav>
        <div className="sidebar-bottom"><div className="sidebar-note"><span className="note-spark">✳</span><p>Your next chapter starts with what you already know.</p><button onClick={() => setActiveView('profile')}>Review your profile <span>↗</span></button></div><div className="account-row"><div className="avatar">{firstName.slice(0, 1).toUpperCase()}</div><div className="account-copy"><strong>{firstName}</strong><span>Personal workspace</span></div><button className="signout" onClick={signOut} aria-label="Sign out" title="Sign out">↗</button></div></div>
      </aside>

      <main className="main-content">
        <header className="topbar"><div className="breadcrumb">Workspace <span>/</span> {({ overview: 'Overview', profile: 'My profile', quiz: 'Career quiz', chat: 'Career guide', history: 'History', roadmap: 'Action plan', compare: 'Compare roles', toolkit: 'Career toolkit', applications: 'Applications' })[activeView]}</div><div className="topbar-right"><span className="status-dot"></span><span>Career map</span><button className="top-avatar" onClick={() => setActiveView('profile')} aria-label="Open profile">{firstName.slice(0, 1).toUpperCase()}</button></div></header>
        {error && <div className="notice notice-error" role="alert">{error}<button onClick={() => setError('')} aria-label="Dismiss">×</button></div>}
        {loading ? <div className="loading-state"><span className="loading-mark">N</span><p>Putting your career map together…</p></div> : (
          activeView === 'quiz' ? <CareerQuiz savedScores={profile?.assessmentScores || {}} onComplete={saveQuiz} saving={saving} /> : activeView === 'chat' ? <ChatPanel onSend={sendChat} /> : activeView === 'roadmap' ? <RoadmapView matches={matches} token={token} request={request} onError={setError} /> : activeView === 'compare' ? <RoleCompareView matches={matches} /> : activeView === 'toolkit' ? <CareerToolkitView matches={matches} profile={profile} token={token} request={request} onError={setError} /> : activeView === 'applications' ? <ApplicationTrackerView token={token} request={request} onError={setError} /> : (
          <>
            {activeView === 'profile' ? <section className="content-page"><div className="page-heading"><span className="eyebrow">The starting point</span><h1>Your profile</h1><p>A few details help us connect your strengths to roles that make sense for you.</p></div>{!profileReady && <div className="notice notice-warm"><span>✳</span> Finish these details to get a more personal match.</div>}{profile && <ProfileForm key={profile.id} profile={profile} onSave={saveProfile} saving={saving} />}</section> : activeView === 'history' ? <section className="content-page"><div className="page-heading"><span className="eyebrow">Your progress</span><h1>Recommendation history</h1><p>Past snapshots of the career paths you explored.</p></div>{history.length ? <div className="history-list">{history.map((item) => <article className="history-row" key={item.id}><div className="history-symbol">↗</div><div><strong>Career match snapshot</strong><span>{new Date(item.generatedAt).toLocaleString()}</span></div><span className="history-role">Role #{item.roleId}</span><strong className="history-score">{Math.round(item.matchScore)}% match</strong></article>)}</div> : <div className="empty-panel"><span className="empty-icon">◷</span><h3>No snapshots yet</h3><p>Generate recommendations from your overview and they will appear here.</p><button className="button button-dark" onClick={() => setActiveView('overview')}>Back to overview <span>↗</span></button></div>}</section> : (
              <div className="overview-page">
                <section className="welcome-row"><div><span className="eyebrow">YOUR CAREER, IN FOCUS</span><h1>Good to see you, {firstName}.</h1><p>Here are a few paths that line up with what you bring.</p></div><button className="button button-outline" onClick={() => setActiveView('profile')}><span aria-hidden="true">✳</span> Edit profile</button></section>
                {!profileReady && <section className="profile-prompt"><div className="prompt-icon">✳</div><div><strong>Make your matches more personal</strong><p>Add your education, skills, and interests to get a more useful career map.</p></div><button className="button button-dark" onClick={() => setActiveView('profile')}>Complete profile <span>↗</span></button></section>}
                <section className="overview-grid"><div className="recommendations-section"><div className="section-heading"><div><span className="eyebrow">A GOOD PLACE TO START</span><h2>Career matches <span className="count-pill">{matches.length}</span></h2></div><label className="search-box"><span aria-hidden="true">⌕</span><input aria-label="Filter career matches" placeholder="Find a role" value={filter} onChange={(event) => setFilter(event.target.value)} /></label></div>
                  <div className="match-list">{filteredMatches.map((match, index) => <button className="match-card" key={match.roleId} onClick={() => openRole(match)}><span className={`role-glyph role-glyph-${index % 4}`}>{match.role.slice(0, 1)}</span><span className="match-info"><span className="match-industry">{match.industry}</span><strong>{match.role}</strong><span className="match-description">{match.description}</span><span className="match-skills">{match.matchedSkills.length} strengths matched <span>·</span> {match.missingSkills.length} to build</span></span><span className="match-score"><strong>{Math.round(match.matchScore)}<small>%</small></strong><span className="score-track"><i style={{ width: `${match.matchScore}%` }} /></span></span><span className="match-arrow">↗</span></button>)}{!filteredMatches.length && <div className="empty-filter">No roles match “{filter}”. Try another search.</div>}</div>
                  <p className="recommendation-disclaimer">Transparent heuristic using skills, interests, education, quiz preferences, and optional academic marks.</p>
                </div><aside className="insight-column"><div className="insight-panel"><span className="eyebrow">YOUR SNAPSHOT</span><h3>A little progress adds up.</h3><div className="snapshot-stat"><span>Roles explored</span><strong>{matches.length}</strong></div><div className="snapshot-stat"><span>Profile details</span><strong>{profileReady ? 'In place' : 'To do'}</strong></div><button onClick={openHistory}>View recommendation history <span>↗</span></button></div><div className="tip-panel"><span className="tip-mark">“</span><p>The best next step is one you can take this week.</p><span className="tip-caption">A note for your career journey</span></div></aside></section>
              </div>
            )}
          </>
          )
        )}
      </main>
      
      {selectedRole && <RoleDialog selectedRole={selectedRole} roleDetail={roleDetail} onClose={() => { setSelectedRole(null); setRoleDetail(null) }} />}
      {toast && <div className="toast" role="status">✓ {toast}</div>}
    </div>
  )
}
export default App

function RoleDialog({ selectedRole, roleDetail, onClose }) {
  const factors = [
    ['Skills', selectedRole.skillFit],
    ['Interests', selectedRole.interestFit],
    ['Education', selectedRole.educationFit],
    ['Career quiz', selectedRole.quizFit],
    ['Academic marks', selectedRole.academicFit],
  ]

  return (
    <div className="dialog-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose() }}>
      <section className="role-dialog" role="dialog" aria-modal="true" aria-labelledby="role-title">
        <button className="dialog-close" onClick={onClose} aria-label="Close role details">×</button>
        <span className="eyebrow">ROLE BREAKDOWN · {selectedRole.industry}</span>
        <h2 id="role-title">{selectedRole.role}</h2>
        <p className="dialog-description">{selectedRole.description}</p>
        <div className="dialog-score">
          <strong>{Math.round(selectedRole.matchScore)}%</strong>
          <span>profile match</span>
          <div className="score-track"><i style={{ width: `${selectedRole.matchScore}%` }} /></div>
        </div>
        <div className="factor-list">
          <span className="eyebrow">WHAT SHAPED THIS MATCH</span>
          {factors.map(([label, score]) => (
            <div className="factor-row" key={label}>
              <span>{label}</span>
              <span className="factor-track"><i style={{ width: `${score ?? 0}%` }} /></span>
              <strong>{Math.round(score ?? 0)}%</strong>
            </div>
          ))}
          <p className="factor-note">Weighted heuristic, not a prediction of job performance or hiring outcomes.</p>
        </div>
        {roleDetail ? <>
          <h3>Skills to focus on</h3>
          <div className="gap-list">
            {roleDetail.gaps.map((gap) => (
              <div className="gap-row" key={gap.skill}>
                <span className={`gap-status ${gap.status.toLowerCase()}`}>{gap.status === 'STRONG' ? '✓' : '•'}</span>
                <div><strong>{gap.skill}</strong><p>{gap.suggestion}</p></div>
                <span className="gap-priority">Priority {gap.priority}</span>
              </div>
            ))}
          </div>
          {roleDetail.roadmap.length > 0 && <div className="roadmap-note"><span>↗</span><div><strong>Your first steps</strong><p>{roleDetail.roadmap.map((item) => item.nextStep).join('. ')}</p></div></div>}
        </> : <div className="detail-loading">Loading role breakdown…</div>}
      </section>
    </div>
  )
}
