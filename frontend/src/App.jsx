import React, { createContext, useContext, useState } from 'react'
import { BrowserRouter, Routes, Route, Navigate, Link, useNavigate } from 'react-router-dom'
import axios from 'axios'
import { StudentDashboard, RecruiterDashboard, TpoDashboard } from './Dashboards'

/* -------------------------------------------------------------------------- */
/*                                AXIOS CLIENT                                */
/* -------------------------------------------------------------------------- */
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

/* -------------------------------------------------------------------------- */
/*                            AUTHENTICATION CONTEXT                          */
/* -------------------------------------------------------------------------- */
const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('user')
    return stored ? JSON.parse(stored) : null
  })

  const login = async (email, password) => {
    const { data } = await api.post('/auth/login', { email, password })
    persist(data)
    return data
  }

  const register = async (payload) => {
    const { data } = await api.post('/auth/register', payload)
    persist(data)
    return data
  }

  const persist = (data) => {
    localStorage.setItem('token', data.token)
    const userObj = { email: data.email, fullName: data.fullName, role: data.role }
    localStorage.setItem('user', JSON.stringify(userObj))
    setUser(userObj)
  }

  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}

/* -------------------------------------------------------------------------- */
/*                             SHARED UI COMPONENTS                           */
/* -------------------------------------------------------------------------- */
const homeByRole = { STUDENT: '/student', TPO: '/tpo', RECRUITER: '/recruiter' }

export function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="navbar">
      <div>
        <Link className="brand" to={user ? homeByRole[user.role] : '/login'}>
          Campus Placement System
        </Link>
      </div>
      <div>
        {user ? (
          <>
            <span style={{ marginRight: '1rem', opacity: 0.85 }}>
              {user.fullName} &middot; {user.role}
            </span>
            <button onClick={handleLogout}>Logout</button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </div>
  )
}

export function ProtectedRoute({ allowedRoles, children }) {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  if (allowedRoles && !allowedRoles.includes(user.role)) return <Navigate to="/" replace />
  return children
}

/* -------------------------------------------------------------------------- */
/*                                 AUTH PAGES                                 */
/* -------------------------------------------------------------------------- */
function Login() {
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

function Register() {
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

function HomeRedirect() {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  return <Navigate to={homeByRole[user.role]} replace />
}

/* -------------------------------------------------------------------------- */
/*                               APPLICATION APP                              */
/* -------------------------------------------------------------------------- */
export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route
            path="/student"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <StudentDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/tpo"
            element={
              <ProtectedRoute allowedRoles={['TPO']}>
                <TpoDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/recruiter"
            element={
              <ProtectedRoute allowedRoles={['RECRUITER']}>
                <RecruiterDashboard />
              </ProtectedRoute>
            }
          />
          <Route path="/" element={<HomeRedirect />} />
          <Route path="*" element={<HomeRedirect />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
