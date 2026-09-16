import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const homeByRole = { STUDENT: '/student', TPO: '/tpo', RECRUITER: '/recruiter' }

export default function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({
    fullName: '', email: '', password: '', role: 'STUDENT', companyName: '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const data = await register(form)
      navigate(homeByRole[data.role] || '/')
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.details?.join(', ') || 'Registration failed.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h2>Create an account</h2>
        {error && <div className="error-banner">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div>
            <label>Full name</label>
            <input name="fullName" required value={form.fullName} onChange={handleChange} />
          </div>
          <div>
            <label>Email</label>
            <input name="email" type="email" required value={form.email} onChange={handleChange} />
          </div>
          <div>
            <label>Password</label>
            <input name="password" type="password" required minLength={6} value={form.password} onChange={handleChange} />
          </div>
          <div>
            <label>Role</label>
            <select name="role" value={form.role} onChange={handleChange}>
              <option value="STUDENT">Student</option>
              <option value="TPO">TPO (Placement Officer)</option>
              <option value="RECRUITER">Recruiter</option>
            </select>
          </div>
          {form.role === 'RECRUITER' && (
            <div>
              <label>Company name</label>
              <input name="companyName" value={form.companyName} onChange={handleChange} />
            </div>
          )}
          <button className="primary" type="submit" disabled={loading}>
            {loading ? 'Creating account...' : 'Register'}
          </button>
        </form>
        <p className="muted" style={{ marginTop: '1rem' }}>
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  )
}
