import { useEffect, useMemo, useState } from 'react'

const APPLICATION_STATUSES = ['SAVED', 'APPLIED', 'INTERVIEW', 'OFFER', 'REJECTED']

function skillMentioned(text, skill) {
  const escaped = skill.trim().replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  if (!escaped) return false
  return new RegExp(`(^|[^a-z0-9])${escaped}($|[^a-z0-9])`, 'i').test(text)
}

function roleSkills(role) {
  return [...new Set([...(role?.matchedSkills || []), ...(role?.missingSkills || [])])]
}

function weeklyCompletionSeries(items) {
  const today = new Date()
  const currentWeek = new Date(today.getFullYear(), today.getMonth(), today.getDate())
  currentWeek.setDate(currentWeek.getDate() - ((currentWeek.getDay() + 6) % 7))
  const weeks = Array.from({ length: 6 }, (_, index) => {
    const date = new Date(currentWeek)
    date.setDate(date.getDate() - (5 - index) * 7)
    return date
  })
  const firstWeek = weeks[0]
  const completedItems = items.filter((item) => item.completed)
  let cumulative = completedItems.filter((item) => !item.completedAt || new Date(item.completedAt) < firstWeek).length

  return weeks.map((week) => {
    const nextWeek = new Date(week)
    nextWeek.setDate(nextWeek.getDate() + 7)
    const weeklyCount = completedItems.filter((item) => {
      if (!item.completedAt) return false
      const completedAt = new Date(item.completedAt)
      return completedAt >= week && completedAt < nextWeek
    }).length
    cumulative += weeklyCount
    return { label: week.toLocaleDateString(undefined, { month: 'short', day: 'numeric' }), total: cumulative }
  })
}

function TaskPerformanceChart({ items, role }) {
  const data = weeklyCompletionSeries(items)
  const maxValue = Math.max(1, ...data.map((point) => point.total))
  const chartPoints = data.map((point, index) => {
    const x = 54 + index * 109.2
    const y = 164 - (point.total / maxValue) * 126
    return { ...point, x, y }
  })
  const tickValues = [...new Set([0, Math.ceil(maxValue / 2), maxValue])]

  return (
    <section className="task-performance" aria-labelledby="task-performance-title">
      <div className="task-performance-heading"><div><span className="eyebrow">Your momentum</span><h2 id="task-performance-title">Completed tasks over time</h2></div><span>{role}</span></div>
      <svg className="task-performance-chart" viewBox="0 0 620 210" role="img" aria-label={`Cumulative completed tasks over the last six weeks for ${role}`}>
        <title>Completed tasks over the last six weeks</title>
        {tickValues.map((value) => {
          const y = 164 - (value / maxValue) * 126
          return <g key={value}><line x1="45" y1={y} x2="605" y2={y} /><text x="34" y={y + 3} textAnchor="end">{value}</text></g>
        })}
        <polyline points={chartPoints.map((point) => `${point.x},${point.y}`).join(' ')} />
        {chartPoints.map((point) => <g key={point.label}><circle cx={point.x} cy={point.y} r="4" /><text x={point.x} y="191" textAnchor="middle">{point.label}</text></g>)}
      </svg>
      {!items.some((item) => item.completed) && <p>Mark a task complete to start building your progress trend.</p>}
    </section>
  )
}

export function RoadmapView({ matches, token, request, onError }) {
  const [roleId, setRoleId] = useState('')
  const selectedRoleId = roleId || String(matches[0]?.roleId || '')
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(false)
  const [updatingId, setUpdatingId] = useState(null)
  const [selectedTask, setSelectedTask] = useState(null)
  const [selectedAnswer, setSelectedAnswer] = useState('')
  const [taskError, setTaskError] = useState('')

  useEffect(() => {
    if (!selectedRoleId) return undefined
    let cancelled = false
    async function loadRoadmap() {
      setLoading(true)
      try {
        await request(`/api/recommendations/roles/${selectedRoleId}`, { token })
        const roadmap = await request(`/api/roadmaps/${selectedRoleId}`, { token })
        if (!cancelled) setItems(roadmap)
      } catch (problem) {
        if (!cancelled) onError(problem.message)
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    loadRoadmap()
    return () => { cancelled = true }
  }, [selectedRoleId, token, request, onError])

  const selectedRole = matches.find((match) => String(match.roleId) === selectedRoleId)
  const selectedChallenge = selectedTask?.challenge
  const completed = items.filter((item) => item.completed).length
  const progress = items.length ? Math.round((completed / items.length) * 100) : 0

  async function submitTaskAnswer() {
    if (!selectedTask?.challenge || selectedTask.completed || !selectedAnswer || updatingId) return
    setUpdatingId(selectedTask.id)
    setTaskError('')
    try {
      await request(`/api/roadmaps/${selectedTask.id}/complete`, {
        token,
        method: 'PATCH',
        body: JSON.stringify({ answer: selectedAnswer }),
      })
      const completedAt = new Date().toISOString()
      setItems((current) => current.map((entry) => entry.id === selectedTask.id ? { ...entry, completed: true, completedAt } : entry))
      setSelectedTask((current) => current?.id === selectedTask.id ? { ...current, completed: true, completedAt } : current)
    } catch (problem) {
      setTaskError(problem.message.includes('(422)')
        ? selectedTask.skill.toLowerCase() === 'sql'
          ? 'Not quite. Group the rows by customer_id, sum amount, then sort by that total descending.'
          : 'Not quite yet. Review the task prompt and try another answer.'
        : problem.message)
    } finally {
      setUpdatingId(null)
    }
  }

  return (
    <section className="content-page feature-page">
      <div className="page-heading feature-heading"><span className="eyebrow">Build momentum</span><h1>Your action plan</h1><p>Turn one role into a sequence of skill-building steps you can track.</p></div>
      {!matches.length ? <div className="empty-panel"><h3>No career matches yet</h3><p>Complete your profile to generate a role-specific action plan.</p></div> : <>
        <div className="feature-controls"><label>Target role<select value={selectedRoleId} onChange={(event) => { setRoleId(event.target.value); setSelectedTask(null) }}>{matches.map((match) => <option key={match.roleId} value={match.roleId}>{match.role}</option>)}</select></label><div className="progress-summary"><div><strong>{progress}%</strong><span>{completed} of {items.length} steps complete</span></div><div className="progress-track"><i style={{ width: `${progress}%` }} /></div></div></div>
        <div className="roadmap-list" aria-live="polite">
          {loading ? <p className="feature-loading">Building your plan…</p> : items.length ? items.map((item, index) => <article className={`roadmap-item ${item.completed ? 'is-complete' : ''}`} key={item.id}><button className="roadmap-item-open" type="button" aria-label={`Open task: ${item.title}`} onClick={() => { setSelectedTask(item); setSelectedAnswer(''); setTaskError('') }}><span className="roadmap-step">{String(index + 1).padStart(2, '0')}</span><span className="roadmap-item-copy"><span className="eyebrow">{item.skill}</span><strong>{item.title}</strong><span>{item.nextStep}</span></span><span className="roadmap-open-label">{item.completed ? 'Completed' : 'Open task'} <span aria-hidden="true">↗</span></span></button></article>) : <div className="empty-panel"><h3>Your plan is clear</h3><p>This role has no outstanding skill steps based on your current profile.</p></div>}
        </div>
        {!loading && items.length > 0 && <TaskPerformanceChart items={items} role={selectedRole?.role || 'Selected role'} />}
        {selectedRole && <p className="feature-footnote">Plan for {selectedRole.role}. Progress is saved to your account.</p>}
      </>}
      {selectedTask && <div className="dialog-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget) setSelectedTask(null) }}><section className="role-dialog roadmap-task-dialog" role="dialog" aria-modal="true" aria-labelledby="roadmap-task-title"><button className="dialog-close" type="button" aria-label="Close task details" onClick={() => setSelectedTask(null)}>×</button><span className="eyebrow">TASK DETAILS · {selectedTask.skill}</span><h2 id="roadmap-task-title">{selectedTask.title}</h2><p className="dialog-description">{selectedTask.nextStep}</p><div className="task-detail-meta"><span>{selectedRole?.role || 'Selected career path'}</span><span>{selectedTask.completed ? 'Completed' : 'In progress'}</span>{selectedTask.completedAt && <span>Finished {new Date(selectedTask.completedAt).toLocaleDateString()}</span>}</div>{selectedTask.completed ? <p className="task-completed-note">This task is complete. Your progress graph includes it.</p> : selectedChallenge ? <fieldset className="task-challenge"><legend>{selectedChallenge.prompt}</legend>{selectedChallenge.options.map((option, index) => { const answer = String.fromCharCode(65 + index); return <label className={selectedAnswer === answer ? 'task-challenge-option selected' : 'task-challenge-option'} key={answer}><input type="radio" name={`task-answer-${selectedTask.id}`} value={answer} checked={selectedAnswer === answer} onChange={() => { setSelectedAnswer(answer); setTaskError('') }} /><span className="task-answer-letter">{answer}</span><span>{option}</span></label>})}</fieldset> : <p className="task-answer-error" role="alert">The task challenge isn’t available yet. Restart the backend to load the latest task exercises.</p>}{taskError && <p className="task-answer-error" role="alert">{taskError}</p>}{!selectedTask.completed && selectedChallenge && <button className="button button-dark task-complete-action" type="button" disabled={!selectedAnswer || updatingId === selectedTask.id} onClick={submitTaskAnswer}>{updatingId === selectedTask.id ? 'Checking…' : 'Check answer & complete'}<span aria-hidden="true">✓</span></button>}</section></div>}
    </section>
  )
}

export function RoleCompareView({ matches }) {
  const [selectedIds, setSelectedIds] = useState([])
  const selected = matches.filter((match) => selectedIds.includes(match.roleId))

  function toggleRole(roleId) {
    setSelectedIds((current) => current.includes(roleId)
      ? current.filter((id) => id !== roleId)
      : current.length < 3 ? [...current, roleId] : current)
  }

  const factors = [
    ['Overall fit', (match) => `${Math.round(match.matchScore)}%`],
    ['Skills already listed', (match) => match.matchedSkills.length],
    ['Skills to build', (match) => match.missingSkills.length],
    ['Industry', (match) => match.industry],
    ['Skill fit', (match) => `${Math.round(match.skillFit)}%`],
    ['Interest fit', (match) => `${Math.round(match.interestFit)}%`],
    ['Education fit', (match) => `${Math.round(match.educationFit)}%`],
    ['Career quiz fit', (match) => `${Math.round(match.quizFit)}%`],
  ]

  return (
    <section className="content-page feature-page">
      <div className="page-heading feature-heading"><span className="eyebrow">Choose with context</span><h1>Compare career paths</h1><p>Look at the match factors and skill gaps side by side. Select up to three roles.</p></div>
      <div className="role-choice-list">{matches.map((match) => <label className="role-choice" key={match.roleId}><input type="checkbox" checked={selectedIds.includes(match.roleId)} disabled={!selectedIds.includes(match.roleId) && selectedIds.length === 3} onChange={() => toggleRole(match.roleId)} /><span><strong>{match.role}</strong><small>{match.industry}</small></span><b>{Math.round(match.matchScore)}%</b></label>)}</div>
      {selected.length > 0 ? <div className="comparison-table" style={{ '--comparison-count': selected.length }}><div className="comparison-row comparison-header"><span>Match factors</span>{selected.map((match) => <strong key={match.roleId}>{match.role}</strong>)}</div>{factors.map(([label, getValue]) => <div className="comparison-row" key={label}><span>{label}</span>{selected.map((match) => <span key={match.roleId}>{getValue(match)}</span>)}</div>)}<div className="comparison-row comparison-skills"><span>Strongest next step</span>{selected.map((match) => <span key={match.roleId}>{match.missingSkills[0] || 'Deepen a listed skill'}</span>)}</div></div> : <div className="comparison-empty">Choose a role above to start comparing.</div>}
      <p className="feature-footnote">Match scores are profile-based heuristics, not predictions of hiring outcomes.</p>
    </section>
  )
}

export function CareerToolkitView({ matches, profile, token, request, onError }) {
  const [mode, setMode] = useState('job')
  const [roleId, setRoleId] = useState('')
  const [text, setText] = useState('')
  const [analysis, setAnalysis] = useState(null)
  const [savedResumes, setSavedResumes] = useState([])
  const [selectedResumeId, setSelectedResumeId] = useState('')
  const [resumeName, setResumeName] = useState('')
  const [resumeBusy, setResumeBusy] = useState(false)
  const [resumeMessage, setResumeMessage] = useState('')
  const selectedRoleId = roleId || String(matches[0]?.roleId || '')
  const selectedRole = matches.find((match) => String(match.roleId) === selectedRoleId)
  const requiredSkills = useMemo(() => roleSkills(selectedRole), [selectedRole])
  const profileSkills = new Set((profile?.skills || []).map((skill) => skill.name.trim().toLowerCase()))

  useEffect(() => {
    let cancelled = false
    request('/api/resumes', { token }).then((resumes) => {
      if (!cancelled) setSavedResumes(resumes)
    }).catch((problem) => {
      if (!cancelled) onError(problem.message)
    })
    return () => { cancelled = true }
  }, [token, request, onError])

  function analyze(event) {
    event.preventDefault()
    if (!selectedRole || !text.trim()) return
    const mentioned = requiredSkills.filter((skill) => skillMentioned(text, skill))
    const evidence = mentioned.filter((skill) => mode === 'job' ? profileSkills.has(skill.toLowerCase()) : true)
    const missing = mentioned.filter((skill) => mode === 'job' && !profileSkills.has(skill.toLowerCase()))
    const notMentioned = requiredSkills.filter((skill) => !mentioned.includes(skill))
    setAnalysis({ roleId: selectedRole.roleId, mentioned, evidence, missing, notMentioned,
      score: mentioned.length ? Math.round((evidence.length / mentioned.length) * 100) : 0 })
  }

  function changeMode(nextMode) {
    setMode(nextMode)
    setAnalysis(null)
  }

  function selectResume(id) {
    setSelectedResumeId(id)
    setResumeMessage('')
    setAnalysis(null)
    const resume = savedResumes.find((item) => String(item.id) === id)
    if (!resume) return
    setResumeName(resume.name)
    setText(resume.content)
    const target = matches.find((match) => match.role === resume.targetRole)
    if (target) setRoleId(String(target.roleId))
  }

  function newResume() {
    setSelectedResumeId('')
    setResumeName('')
    setText('')
    setAnalysis(null)
    setResumeMessage('')
  }

  async function saveResume() {
    if (!resumeName.trim() || !text.trim()) return
    setResumeBusy(true)
    setResumeMessage('')
    try {
      const payload = { name: resumeName.trim(), targetRole: selectedRole?.role || null, content: text.trim() }
      const saved = await request(selectedResumeId ? `/api/resumes/${selectedResumeId}` : '/api/resumes', {
        token,
        method: selectedResumeId ? 'PUT' : 'POST',
        body: JSON.stringify(payload),
      })
      setSavedResumes((current) => [saved, ...current.filter((resume) => resume.id !== saved.id)])
      setSelectedResumeId(String(saved.id))
      setResumeMessage('Saved to your private resume library.')
    } catch (problem) {
      onError(problem.message)
    } finally {
      setResumeBusy(false)
    }
  }

  async function deleteResume() {
    if (!selectedResumeId) return
    setResumeBusy(true)
    try {
      await request(`/api/resumes/${selectedResumeId}`, { token, method: 'DELETE' })
      setSavedResumes((current) => current.filter((resume) => String(resume.id) !== selectedResumeId))
      newResume()
    } catch (problem) {
      onError(problem.message)
    } finally {
      setResumeBusy(false)
    }
  }

  return (
    <section className="content-page feature-page">
      <div className="page-heading feature-heading"><span className="eyebrow">Prepare with evidence</span><h1>Career toolkit</h1><p>Check a job description against your profile, or review a resume against one target role.</p></div>
      <div className="tool-tabs" role="tablist" aria-label="Career toolkit mode"><button className={mode === 'job' ? 'selected' : ''} role="tab" aria-selected={mode === 'job'} onClick={() => changeMode('job')}>Job-post match</button><button className={mode === 'resume' ? 'selected' : ''} role="tab" aria-selected={mode === 'resume'} onClick={() => changeMode('resume')}>Resume / portfolio check</button></div>
      {mode === 'resume' && <div className="resume-library-bar"><label>Saved resumes<select aria-label="Saved resumes" value={selectedResumeId} onChange={(event) => selectResume(event.target.value)}><option value="">Choose a saved resume</option>{savedResumes.map((resume) => <option key={resume.id} value={resume.id}>{resume.name}{resume.targetRole ? ` · ${resume.targetRole}` : ''}</option>)}</select></label><div><button className="button button-outline button-small" type="button" onClick={newResume}>New draft</button>{selectedResumeId && <button className="button button-outline button-small resume-delete-button" type="button" disabled={resumeBusy} onClick={deleteResume}>Delete saved resume</button>}</div></div>}
      <form className="tool-form" onSubmit={analyze}>
        <label>Target role<select value={selectedRoleId} onChange={(event) => { setRoleId(event.target.value); setAnalysis(null) }} disabled={!matches.length}>{matches.map((match) => <option key={match.roleId} value={match.roleId}>{match.role}</option>)}</select></label>
        {mode === 'resume' && <label>Resume name<input value={resumeName} onChange={(event) => setResumeName(event.target.value)} maxLength="160" placeholder="e.g. Software engineering resume" required /></label>}
        <label>{mode === 'job' ? 'Job description' : 'Resume or portfolio text'}<textarea value={text} onChange={(event) => { setText(event.target.value); setAnalysis(null) }} rows="9" maxLength="10000" placeholder={mode === 'job' ? 'Paste the job description here…' : 'Paste resume sections or a portfolio project description…'} required /></label>
        <div className="tool-submit-row"><span>{text.length.toLocaleString()} / 10,000 characters</span><div className="resume-actions">{mode === 'resume' && <button className="button button-outline" type="button" disabled={resumeBusy || !resumeName.trim() || !text.trim()} onClick={saveResume}>{resumeBusy ? 'Saving…' : selectedResumeId ? 'Update saved resume' : 'Save resume'}</button>}<button className="button button-dark" type="submit" disabled={!selectedRole || !text.trim()}>{mode === 'job' ? 'Check fit' : 'Review evidence'} <span>↗</span></button></div></div>
      </form>
      {mode === 'resume' && resumeMessage && <p className="resume-save-message" role="status">{resumeMessage}</p>}
      {analysis && analysis.roleId === selectedRole?.roleId && <section className="analysis-result" aria-live="polite"><div className="analysis-result-heading"><div><span className="eyebrow">{mode === 'job' ? 'Profile coverage' : 'Role evidence'}</span><h2>{selectedRole.role}</h2></div>{mode === 'job' && <strong>{analysis.score}%<small> of mentioned skills already in your profile</small></strong>}</div>
        <div className="analysis-columns"><div><h3>{mode === 'job' ? 'Skills you list' : 'Required skills found in text'}</h3>{analysis.evidence.length ? <ul>{analysis.evidence.map((skill) => <li key={skill}>{skill}</li>)}</ul> : <p>No exact skill-name matches yet.</p>}</div>{mode === 'job' ? <div><h3>Potential gaps</h3>{analysis.missing.length ? <ul>{analysis.missing.map((skill) => <li key={skill}>{skill}</li>)}</ul> : <p>No gaps among the skills detected.</p>}</div> : <div><h3>Still to evidence</h3>{analysis.notMentioned.length ? <ul>{analysis.notMentioned.map((skill) => <li key={skill}>{skill}</li>)}</ul> : <p>All listed requirements were found.</p>}</div>}</div>
        {mode === 'job' && analysis.notMentioned.length > 0 && <p className="analysis-note">The posting did not explicitly mention: {analysis.notMentioned.join(', ')}.</p>}
        {mode === 'resume' && <p className="analysis-note">Add a skill only when it reflects real experience. Strengthen each match with a specific project, result, or example.</p>}
      </section>}
      <p className="feature-footnote">{mode === 'job' ? 'Job-post text is analyzed in this browser and is not uploaded or saved.' : 'Resume text stays in your browser unless you choose Save resume; saved text is private to your account.'} Matching uses explicit skill-name mentions, not semantic resume evaluation.</p>
    </section>
  )
}

export function ApplicationTrackerView({ token, request, onError }) {
  const [applications, setApplications] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [filter, setFilter] = useState('ALL')
  const [draft, setDraft] = useState({ role: '', company: '', postingUrl: '', appliedDate: '', notes: '' })
  const [updatingId, setUpdatingId] = useState(null)

  useEffect(() => {
    let cancelled = false
    request('/api/applications', { token }).then((items) => {
      if (!cancelled) setApplications(items)
    }).catch((problem) => {
      if (!cancelled) onError(problem.message)
    }).finally(() => {
      if (!cancelled) setLoading(false)
    })
    return () => { cancelled = true }
  }, [token, request, onError])

  const visibleApplications = filter === 'ALL' ? applications : applications.filter((item) => item.status === filter)
  const counts = Object.fromEntries(APPLICATION_STATUSES.map((status) => [status, applications.filter((item) => item.status === status).length]))

  async function addApplication(event) {
    event.preventDefault()
    setSaving(true)
    try {
      const created = await request('/api/applications', { token, method: 'POST', body: JSON.stringify({
        ...draft, appliedDate: draft.appliedDate || null,
      }) })
      setApplications((current) => [created, ...current])
      setDraft({ role: '', company: '', postingUrl: '', appliedDate: '', notes: '' })
    } catch (problem) {
      onError(problem.message)
    } finally {
      setSaving(false)
    }
  }

  async function updateApplication(item, changes) {
    setUpdatingId(item.id)
    try {
      const updated = await request(`/api/applications/${item.id}`, { token, method: 'PATCH', body: JSON.stringify({
        status: changes.status ?? item.status,
        appliedDate: changes.appliedDate ?? item.appliedDate,
        notes: changes.notes ?? item.notes,
      }) })
      setApplications((current) => current.map((entry) => entry.id === item.id ? updated : entry))
    } catch (problem) {
      onError(problem.message)
    } finally {
      setUpdatingId(null)
    }
  }

  async function deleteApplication(item) {
    try {
      await request(`/api/applications/${item.id}`, { token, method: 'DELETE' })
      setApplications((current) => current.filter((entry) => entry.id !== item.id))
    } catch (problem) {
      onError(problem.message)
    }
  }

  return (
    <section className="content-page feature-page">
      <div className="page-heading feature-heading"><span className="eyebrow">Keep the process moving</span><h1>Application tracker</h1><p>Keep roles, dates, and follow-ups in one private place.</p></div>
      <div className="application-counts"><button className={filter === 'ALL' ? 'selected' : ''} onClick={() => setFilter('ALL')}><strong>{applications.length}</strong><span>All</span></button>{APPLICATION_STATUSES.map((status) => <button className={filter === status ? 'selected' : ''} key={status} onClick={() => setFilter(status)}><strong>{counts[status]}</strong><span>{statusLabel(status)}</span></button>)}</div>
      <form className="application-form" onSubmit={addApplication}><h2>Add an opportunity</h2><div className="application-form-grid"><label>Role<input maxLength="120" value={draft.role} onChange={(event) => setDraft({ ...draft, role: event.target.value })} required placeholder="e.g. Product designer" /></label><label>Company<input maxLength="120" value={draft.company} onChange={(event) => setDraft({ ...draft, company: event.target.value })} required placeholder="Company name" /></label><label>Posting link<input type="url" maxLength="2048" value={draft.postingUrl} onChange={(event) => setDraft({ ...draft, postingUrl: event.target.value })} placeholder="https://…" /></label><label>Applied date<input type="date" value={draft.appliedDate} onChange={(event) => setDraft({ ...draft, appliedDate: event.target.value })} /></label></div><label>Notes<textarea maxLength="1500" rows="2" value={draft.notes} onChange={(event) => setDraft({ ...draft, notes: event.target.value })} placeholder="Contact, next step, or interview notes" /></label><div className="application-form-actions"><span>New entries start in Saved.</span><button className="button button-dark" type="submit" disabled={saving}>{saving ? 'Adding…' : 'Add to tracker'} <span>↗</span></button></div></form>
      <div className="application-list" aria-live="polite"><div className="application-list-heading"><h2>{filter === 'ALL' ? 'Your opportunities' : statusLabel(filter)}</h2><span>{visibleApplications.length} entries</span></div>{loading ? <p className="feature-loading">Loading applications…</p> : visibleApplications.length ? visibleApplications.map((item) => <article className="application-row" key={item.id}><div className="application-row-main"><span className="application-company-mark">{item.company.slice(0, 1).toUpperCase()}</span><div><strong>{item.role}</strong><span>{item.company}{item.appliedDate ? ` · ${item.appliedDate}` : ''}</span>{item.postingUrl && <a href={item.postingUrl} target="_blank" rel="noreferrer">Open posting ↗</a>}</div></div><select aria-label={`Status for ${item.role} at ${item.company}`} value={item.status} disabled={updatingId === item.id} onChange={(event) => updateApplication(item, { status: event.target.value })}>{APPLICATION_STATUSES.map((status) => <option key={status} value={status}>{statusLabel(status)}</option>)}</select><details className="application-notes"><summary>{item.notes ? 'Edit notes' : 'Add notes'}</summary><ApplicationNotes item={item} onSave={(changes) => updateApplication(item, changes)} /></details><button className="application-delete" type="button" aria-label={`Remove ${item.role} at ${item.company}`} title="Remove entry" onClick={() => deleteApplication(item)}>×</button></article>) : <div className="comparison-empty">{applications.length ? 'No opportunities in this stage yet.' : 'Your saved opportunities will appear here.'}</div>}</div>
    </section>
  )
}

function ApplicationNotes({ item, onSave }) {
  const [notes, setNotes] = useState(item.notes || '')
  const [appliedDate, setAppliedDate] = useState(item.appliedDate || '')
  return <form className="application-note-form" onSubmit={(event) => { event.preventDefault(); onSave({ notes, appliedDate: appliedDate || null }) }}><label>Applied date<input type="date" value={appliedDate} onChange={(event) => setAppliedDate(event.target.value)} /></label><label>Notes<textarea rows="3" maxLength="1500" value={notes} onChange={(event) => setNotes(event.target.value)} /></label><button className="button button-outline button-small" type="submit">Save details</button></form>
}

function statusLabel(status) {
  return status.charAt(0) + status.slice(1).toLowerCase()
}