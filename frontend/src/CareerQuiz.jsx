import { useState } from 'react'

const SCALE = [
  { value: 1, label: 'Not me' },
  { value: 2, label: 'A little' },
  { value: 3, label: 'Sometimes' },
  { value: 4, label: 'Often' },
  { value: 5, label: 'Very me' },
]

const QUESTIONS = [
  { category: 'technical', text: 'I enjoy finding out how software, devices, or systems work.' },
  { category: 'technical', text: 'I like learning tools by experimenting and building something.' },
  { category: 'analytical', text: 'I enjoy spotting patterns in information and explaining what they mean.' },
  { category: 'analytical', text: 'I like comparing evidence before choosing a solution.' },
  { category: 'creative', text: 'I enjoy shaping words, visuals, or experiences to communicate an idea.' },
  { category: 'creative', text: 'I like having room to invent different ways to solve a problem.' },
  { category: 'people', text: 'I get energy from helping someone learn, decide, or feel supported.' },
  { category: 'people', text: 'I enjoy listening carefully and understanding what people need.' },
  { category: 'leadership', text: 'I like coordinating a group and keeping shared work moving.' },
  { category: 'leadership', text: 'I am comfortable making a plan when priorities compete.' },
  { category: 'hands-on', text: 'I prefer making, testing, fixing, or working with things in the real world.' },
  { category: 'hands-on', text: 'I enjoy seeing a physical result at the end of a project.' },
]

function scoreAnswers(answers) {
  return Object.fromEntries([...new Set(QUESTIONS.map((question) => question.category))].map((category) => {
    const categoryAnswers = QUESTIONS.map((question, index) => question.category === category ? answers[index] : null).filter(Number.isFinite)
    const average = categoryAnswers.reduce((sum, answer) => sum + answer, 0) / categoryAnswers.length
    return [category, Math.round(((average - 1) / 4) * 100)]
  }))
}

export default function CareerQuiz({ savedScores = {}, onComplete, saving }) {
  const [answers, setAnswers] = useState({})
  const answeredCount = Object.keys(answers).length
  const complete = answeredCount === QUESTIONS.length

  function submit(event) {
    event.preventDefault()
    if (!complete) return
    onComplete(scoreAnswers(answers))
  }

  return (
    <section className="content-page quiz-page">
      <div className="page-heading">
        <span className="eyebrow">A SHORT SELF-ASSESSMENT</span>
        <h1>Career fit quiz</h1>
        <p>Choose what feels most like you. Your answers add a work-preference signal to your role matches.</p>
      </div>
      {Object.keys(savedScores).length > 0 && <div className="saved-scores"><span className="eyebrow">LATEST RESULTS</span><div className="score-chips">{Object.entries(savedScores).map(([category, score]) => <span key={category}>{category.replace('-', ' ')} <strong>{score}%</strong></span>)}</div></div>}
      <form onSubmit={submit}>
        <div className="quiz-progress"><span>{answeredCount} of {QUESTIONS.length} answered</span><div className="quiz-progress-track"><i style={{ width: `${(answeredCount / QUESTIONS.length) * 100}%` }} /></div></div>
        <div className="quiz-questions">{QUESTIONS.map((question, index) => (
          <fieldset className="quiz-question" key={question.text}>
            <legend><span>{String(index + 1).padStart(2, '0')}</span>{question.text}</legend>
            <div className="quiz-scale">{SCALE.map((option) => <label className={answers[index] === option.value ? 'quiz-choice selected' : 'quiz-choice'} key={option.value}><input type="radio" name={`question-${index}`} value={option.value} checked={answers[index] === option.value} onChange={() => setAnswers((current) => ({ ...current, [index]: option.value }))} /><span className="choice-number">{option.value}</span><span className="choice-label">{option.label}</span></label>)}</div>
          </fieldset>
        ))}</div>
        <div className="quiz-actions"><span>{complete ? 'Ready to score your results.' : `Answer ${QUESTIONS.length - answeredCount} more to finish.`}</span><div><button className="button button-outline" type="button" onClick={() => setAnswers({})}>Start over</button><button className="button button-dark" type="submit" disabled={!complete || saving}>{saving ? 'Saving results…' : 'Save quiz results'}<span aria-hidden="true">↗</span></button></div></div>
      </form>
    </section>
  )
}
