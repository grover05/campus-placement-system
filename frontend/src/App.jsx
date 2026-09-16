import React from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import Login from './pages/Login'
import Register from './pages/Register'
import StudentDashboard from './pages/student/StudentDashboard'
import TpoDashboard from './pages/tpo/TpoDashboard'
import RecruiterDashboard from './pages/recruiter/RecruiterDashboard'

const homeByRole = { STUDENT: '/student', TPO: '/tpo', RECRUITER: '/recruiter' }

function HomeRedirect() {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  return <Navigate to={homeByRole[user.role]} replace />
}

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
