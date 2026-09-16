import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const homeByRole = { STUDENT: '/student', TPO: '/tpo', RECRUITER: '/recruiter' }

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const data = await login(form.email, form.password)
      navigate(homeByRole[data.role] || '/')
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Check your credentials.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h2>Sign in</h2>
        <p className="muted">Campus Placement Management System</p>
        {error && <div className="error-banner">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div>
            <label>Email</label>
            <input name="email" type="email" required value={form.email} onChange={handleChange} />
          </div>
          <div>
            <label>Password</label>
            <input name="password" type="password" required value={form.password} onChange={handleChange} />
          </div>
          <button className="primary" type="submit" disabled={loading}>
            {loading ? 'Signing in...' : 'Sign in'}
          </button>
        </form>
        <p className="muted" style={{ marginTop: '1rem' }}>
          Don&apos;t have an account? <Link to="/register">Register</Link>
        </p>
      </div>
    </div>
  )
}
